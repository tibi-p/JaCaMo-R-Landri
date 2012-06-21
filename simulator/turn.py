from django.db.models import F
from envuser.models import EnvAgent
from simulator.sandbox import JaCaMoSandbox
from schedule.models import Schedule
from subenvironment.models import SubEnvironment, DefaultExtra
from multiprocessing import Pipe, Process
import os
import tempfile
from solution.specification import SolutionSpecification

def runTurn(numSteps):
    for step in xrange(numSteps):
        runStep(step)
    EnvAgent.objects.all().update(timePool=None)

def getSandboxProcess(subenvironment, schedules, usePipe=False):
    masArgs = {
        'name': 'subenv_%d' % (subenvironment.pk,),
        'infra': "Centralised",
        'env': "c4jason.CartagoEnvironment",
        #'env': "Env(2, 2000)",
        'agents': [ ],
    }
    solutions = schedules.get_solutions()
    for schedule in schedules:
        solution = schedule.solution
        envUser = solution.envUser
        xmlFile = solution.xmlFile
        
        spec = SolutionSpecification()
        agents,agentFiles,artifactsJar,orgsZip = spec.parse_agents(xmlFile)
        masArgs['agents']=agents

    conn = None
    args = (subenvironment, solutions, masArgs)
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
            print step, subEnvironment, schedules, schedules.get_solutions()
            process, _ = getSandboxProcess(subEnvironment, schedules)
            for envUser in schedules.get_envusers():
                for envAgent in envUser.envagent_set.all():
                    envAgent.timePool = F('timePool') - 1
                    envAgent.save()
            for envUser in schedules.get_envusers():
                for envAgent in envUser.envagent_set.all():
                    print envAgent.timePool
            process.start()

def runInSandbox(subenvironment, solutions, masArgs, pipe=None):
    rootDir = tempfile.mkdtemp()
    sandbox = JaCaMoSandbox(rootDir)
    sandbox.populate((solution.file.path for solution in solutions), {
        'agents': getPathList(subenvironment, 'agent_set'),
        'artifacts': getPathList(subenvironment, 'artifact_set'),
        'orgs': getPathList(subenvironment, 'organization_set'),
        '..': querysetToPaths(DefaultExtra.objects.all()),
    })
    masFilename = sandbox.writeMAS(**masArgs)
    sandbox.buildMAS(masFilename)
    sandbox.ant(pipe)
    sandbox.clean()

def canRunSubEnvironment(subEnvironment, tests):
    return sum(test.numAgents for test in tests) >= 1

def getPathList(subenvironment, key_set):
    queryset = getattr(subenvironment, key_set).all()
    return querysetToPaths(queryset)

def querysetToPaths(queryset):
    return (elem.file.path for elem in queryset)
