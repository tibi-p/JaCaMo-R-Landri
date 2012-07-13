from django.db.models import F
from envuser.models import EnvAgent
from simulator.sandbox import JaCaMoSandbox
from schedule.models import Schedule
from solution.specification import SolutionSpecification
from subenvironment.models import SubEnvironment, DefaultExtra
from multiprocessing import Pipe, Process
import tempfile

def runTurn(numSteps):
    for step in xrange(numSteps):
        runStep(step)
    defaultValue = None
    for field in EnvAgent._meta.fields: #@UndefinedVariable
        if field.name == 'timePool':
            defaultValue = field.default
            break
    if defaultValue is not None:
        EnvAgent.objects.all().update(timePool=defaultValue)

def getSandboxProcess(subenvironment, solutions, usePipe=False):
    masArgs = {
        'name': 'subenv_%d' % (subenvironment.pk,),
        'infra': "Centralised",
        'env': "c4jason.CartagoEnvironment",
        #'env': "Env(2, 2000)",
        'agents': [ ],
    }
    solutionFiles = {
        'agents': [ ],
        'artifacts': [ ],
        'orgs': [ ],
    }
    specs = [ ]
    for solution in solutions:
        envUser = solution.envUser
        agents, artifacts, orgs = SolutionSpecification.parse_repair_xml(solution)
        specs.append(agents)

        solutionFiles['agents'].append(solution.agents.path)
        if artifacts:
            solutionFiles['artifacts'].append(artifacts)
        if orgs:
            solutionFiles['orgs'].append(orgs)
    agents = [ elem for row in specs for elem in row ]
    agents.append({
        'arch': 'c4jason.CAgentArch',
        'name': 'prime_agent_s_%s' % (subenvironment.envType,),
        'no': 1,
    })
    masArgs['agents'] = agents

    conn = None
    args = (subenvironment, solutionFiles, masArgs)
    if usePipe:
        conn = Pipe()
        args += (conn[1],)
    return (Process(target=runInSandbox, args=args), conn)

def runStep(step):
    for subEnvironment in SubEnvironment.objects.all():
        schedules = Schedule.objects.filter(**{
            'solution__subEnvironment': subEnvironment,
            'step': step,
        })
        if canRunSubEnvironment(subEnvironment, schedules):
            process, _ = getSandboxProcess(subEnvironment, schedules.get_solutions())
            for envUser in schedules.get_envusers():
                for envAgent in envUser.envagent_set.all():
                    envAgent.timePool = F('timePool') - 1
                    envAgent.save()
            for envUser in schedules.get_envusers():
                for envAgent in envUser.envagent_set.all():
                    print envAgent.timePool
            process.start()

def runInSandbox(subenvironment, solutionFiles, masArgs, pipe=None):
    rootDir = tempfile.mkdtemp()
    sandbox = JaCaMoSandbox(rootDir, subenvironment)
    sandbox.populate(solutionFiles, {
        'agents': getPathList(subenvironment, 'agent_set'),
        'artifacts': getPathList(subenvironment, 'artifact_set'),
        'orgs': getPathList(subenvironment, 'organization_set'),
        '..': querysetToPaths(DefaultExtra.objects.all()),
    })
    masFilename = sandbox.writeMAS(**masArgs)
    sandbox.buildMAS(masFilename)
    sandbox.ant(pipe)
    sandbox.clean()

def canRunSubEnvironment(subEnvironment, schedules):
    return schedules.count() > 0

def getPathList(subenvironment, key_set):
    queryset = getattr(subenvironment, key_set).all()
    return querysetToPaths(queryset)

def querysetToPaths(queryset):
    return (elem.file.path for elem in queryset)
