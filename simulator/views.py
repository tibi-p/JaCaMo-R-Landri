from django import forms
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext
from schedule.models import OfflineTest
from schedule.models import Schedule
from simulator.turn import getSandboxProcess, runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
import json

class SimulateForm(forms.Form):
    subenvironment = forms.ModelChoiceField(queryset=SubEnvironment.objects.none(),
        required=False)

    def __init__(self, *args, **kwargs):
        currentUser = kwargs.pop('user', None)
        super(SimulateForm, self).__init__(*args, **kwargs)
        if currentUser != None:
            subEnvFilter = { 'solution__offlinetest__isnull': False }
            if not currentUser.is_superuser:
                subEnvFilter['solution__envUser__user__id'] = currentUser.id
            subEnvQueryset = SubEnvironment.objects.filter(**subEnvFilter).distinct()
            self.fields['subenvironment'] = forms.ModelChoiceField(queryset=subEnvQueryset,
                required=False)

@login_required
def run(request):
    runTurn(5)
    subEnvironmentList = SubEnvironment.objects.all()
    return render_to_response('simulator/run.html',
        { 'subEnvironmentList': subEnvironmentList },
        context_instance = RequestContext(request))

@login_required
def simulate(request):
    if request.method == 'POST':
        form = SimulateForm(request.POST, user=request.user)
        if form.is_valid():
            cleaned_data = form.cleaned_data
            subenvironment = cleaned_data['subenvironment']
            offlineTests = queryOfflineTests(request.user, subenvironment.id)

            process = getSandboxProcess(subenvironment, offlineTests)
            process.start()
            return HttpResponseRedirect('/simulator/simulate/')
    else:
        form = SimulateForm(user=request.user)

    return render_to_response('simulator/simulate.html',
        { 'form': form },
        context_instance = RequestContext(request))

@login_required
def getsolutions(request, subEnvId):
    offlineTests = queryOfflineTests(request.user, subEnvId)
    solutions = offlineTests.get_solutions()
    jsonObj = { }
    jsonObj['offlineTests'] = serializers.serialize("json", offlineTests)
    jsonObj['solutions'] = serializers.serialize("json", solutions)
    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")

def queryOfflineTests(user, subEnvId):
    offlineTestFilter = { }
    if not user.is_superuser:
        offlineTestFilter['solution__envUser__user'] = user
    if subEnvId:
        offlineTestFilter['solution__subEnvironment__id'] = subEnvId
    offlineTests = OfflineTest.objects.filter(**offlineTestFilter)
    return offlineTests
