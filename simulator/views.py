from django import forms
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.core import serializers
from django.http import HttpResponse
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvUser
from schedule.models import Schedule
from schedule.models import FakeSchedule
from solution.models import Solution
from subenvironment.models import SubEnvironment
from django.forms.formsets import formset_factory
from django.forms.models import modelformset_factory
import json

FakeScheduleFormSet = modelformset_factory(FakeSchedule, extra=0)

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

@login_required
def simulate(request):
    if request.method == 'POST':
        form = SimulateForm(request.POST, user=request.user)
        if form.is_valid():
            print form.cleaned_data
            return
    else:
        form = SimulateForm(user=request.user)

    return render_to_response('simulator/simulate.html',
        { 'form': form },
        context_instance = RequestContext(request))

@login_required
def getsolutions(request, subEnvId):
    fakeScheduleFilter = { }
    currentUser = request.user
    if not currentUser.is_superuser:
        fakeScheduleFilter['solution__envUser__user__id'] = currentUser.id
    if subEnvId:
        fakeScheduleFilter['solution__subEnvironment__id'] = subEnvId
    fakeSchedules = FakeSchedule.objects.filter(**fakeScheduleFilter)

    solutions = set()
    for fakeSchedule in fakeSchedules:
        solutions.add(fakeSchedule.solution)
    solutions = list(solutions)

    jsonObj = { }
    jsonObj['fakeSchedules'] = serializers.serialize("json", fakeSchedules)
    jsonObj['solutions'] = serializers.serialize("json", solutions)
    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")
