from simulator.sandbox import JaCaMoSandbox
from schedule.models import Schedule
from subenvironment.models import SubEnvironment
from multiprocessing import Process
import os
import tempfile

def runTurn(numSteps):
    for step in xrange(numSteps):
        runStep(step)

def getSandboxProcess(subenvironment, solutions):
    masArgs = {
        'name': "house_building",
        'infra': "Centralised",
        'env': "c4jason.CartagoEnvironment",
        'agents': { },
    }
    for solution in solutions:
        filename = os.path.basename(solution.file.name)
        agentName = os.path.splitext(filename)[0]
        count = sum(offlineTest.numAgents for offlineTest
            in solution.offlinetest_set.all())
        masArgs['agents'][agentName] = {
            'arch': 'c4jason.CAgentArch',
            'no': count,
        }

    return Process(target=runInSandbox,
        args=(subenvironment, solutions, masArgs))

def runInSandbox(subenvironment, solutions, masArgs):
    rootDir = tempfile.mkdtemp()
    sandbox = JaCaMoSandbox(rootDir)
    sandbox.populate((solution.file.path for solution in solutions), {
        'agents': getPathList(subenvironment, 'envagent_set'),
        'artifacts': getPathList(subenvironment, 'artifact_set'),
        'orgs': getPathList(subenvironment, 'organization_set'),
    })
    sandbox = JaCaMoSandbox(rootDir)
    masFilename = sandbox.writeMAS(**masArgs)
    sandbox.buildMAS(masFilename)
    sandbox.ant()
    sandbox.clean()

def runStep(step):
    for subEnvironment in SubEnvironment.objects.all():
        schedules = Schedule.objects.filter(**{
            'solution__subEnvironment': subEnvironment,
            'step': step,
        })
        if canRunSubEnvironment(subEnvironment, schedules):
            print step, subEnvironment, schedules

def canRunSubEnvironment(subEnvironment, offlineTests):
    return sum(offlineTest.numAgents for offlineTest in offlineTests) >= 1

def getPathList(subenvironment, key_set):
    return ( elem.file.path for elem in getattr(subenvironment, key_set).all() )
