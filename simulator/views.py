from django import forms
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext
from schedule.models import Schedule
from schedule.models import FakeSchedule
from simulator.sandbox import JaCaMoSandbox
from solution.models import Solution
from subenvironment.models import SubEnvironment
import json
from multiprocessing import Process
import os
import tempfile

class SimulateForm(forms.Form):
    subenvironment = forms.ModelChoiceField(queryset=SubEnvironment.objects.none(),
        required=False)

    def __init__(self, *args, **kwargs):
        currentUser = kwargs.pop('user', None)
        super(SimulateForm, self).__init__(*args, **kwargs)
        if currentUser != None:
            subEnvFilter = { 'solution__fakeschedule__isnull': False }
            if not currentUser.is_superuser:
                subEnvFilter['solution__envUser__user__id'] = currentUser.id
            subEnvQueryset = SubEnvironment.objects.filter(**subEnvFilter).distinct()
            self.fields['subenvironment'] = forms.ModelChoiceField(queryset=subEnvQueryset,
                required=False)

@login_required
def run(request):
    subEnvironmentList = SubEnvironment.objects.all()
    return render_to_response('simulator/run.html',
        { 'subEnvironmentList': subEnvironmentList },
        context_instance = RequestContext(request))

def getPathList(subenvironment, key_set):
    return ( elem.file.path for elem in getattr(subenvironment, key_set).all() )

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

@login_required
def simulate(request):
    if request.method == 'POST':
        form = SimulateForm(request.POST, user=request.user)
        if form.is_valid():
            cleaned_data = form.cleaned_data
            subenvironment = cleaned_data['subenvironment']
            fakeSchedules, solutions = querySolutions(request.user, subenvironment.id)

            masArgs = {
                'name': "house_building",
                'infra': "Centralised",
                'env': "c4jason.CartagoEnvironment",
                'agents': { },
            }
            for solution in solutions:
                filename = os.path.basename(solution.file.name)
                agentName = os.path.splitext(filename)[0]
                count = sum(fakeSchedule.numAgents for fakeSchedule
                    in solution.fakeschedule_set.all())
                masArgs['agents'][agentName] = {
                    'arch': 'c4jason.CAgentArch',
                    'no': count,
                }

            Process(target=runInSandbox, args=(subenvironment, solutions, masArgs)).start()
            return HttpResponseRedirect('/simulator/simulate/')
    else:
        form = SimulateForm(user=request.user)

    return render_to_response('simulator/simulate.html',
        { 'form': form },
        context_instance = RequestContext(request))

@login_required
def getsolutions(request, subEnvId):
    fakeSchedules, solutions = querySolutions(request.user, subEnvId)
    jsonObj = { }
    jsonObj['fakeSchedules'] = serializers.serialize("json", fakeSchedules)
    jsonObj['solutions'] = serializers.serialize("json", solutions)
    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")

def querySolutions(user, subEnvId):
    fakeScheduleFilter = { }
    if not user.is_superuser:
        fakeScheduleFilter['solution__envUser__user__id'] = user.id
    if subEnvId:
        fakeScheduleFilter['solution__subEnvironment__id'] = subEnvId
    fakeSchedules = FakeSchedule.objects.filter(**fakeScheduleFilter)

    solutions = set()
    for fakeSchedule in fakeSchedules:
        solutions.add(fakeSchedule.solution)

    return fakeSchedules, list(solutions)
