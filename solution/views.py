from django import forms
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext
from schedule.models import OfflineTest, Schedule
from simulator.turn import getSandboxProcess, runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
import json

class SolutionForm(forms.ModelForm):
    class Meta:
        model = Solution
        exclude = ('subEnvironment',)

@login_required
def index(request):
    if request.method == 'POST':
        form = SolutionForm(request.POST)
        if form.is_valid():
            cleaned_data = form.cleaned_data
            print cleaned_data
            return HttpResponseRedirect('/solution/index/')
    else:
        form = SolutionForm()

    return render_to_response('solution/index.html',
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
