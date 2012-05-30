from django import forms
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from home.base import make_base_custom_formset
from schedule.models import OfflineTest, Schedule
from simulator.turn import getSandboxProcess, runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
import json
import os

class OfflineTestForm(forms.models.ModelForm):
    class Meta:
        model = OfflineTest

def make_radio_offline_test_form(testKwArgs={ }):
    queryset = testKwArgs.get('queryset', None)
    if queryset is None:
        queryset = Solution.objects.all()
        testKwArgs['queryset'] = queryset
    if testKwArgs.get('initial', None) is None:
        if queryset:
            testKwArgs['initial'] = queryset[0]

    class RadioOfflineTestForm(OfflineTestForm):
        solution = forms.models.ModelChoiceField(**testKwArgs)

    return RadioOfflineTestForm

class StiffOfflineTestForm(OfflineTestForm):
    solution = forms.models.ModelChoiceField(Solution.objects.all(),
        widget=forms.TextInput(attrs={
            'class': 'disabled',
            'readonly': 'readonly',
        }))

def make_offline_test_formset(subEnvId, extra):
    tests = OfflineTest.objects.filter(solution__subEnvironment__id=subEnvId)
    BaseOfflineTestFormSet = make_base_custom_formset(tests)
    return modelformset_factory(OfflineTest, form=StiffOfflineTestForm,
        formset=BaseOfflineTestFormSet, extra=extra)

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
        { },
        context_instance = RequestContext(request))

@login_required
def simulate(request):
    return simulate_common(request)

@login_required
def simulate_post(request, subEnvId):
    subEnvironment = SubEnvironment.objects.get(pk=subEnvId)
    return simulate_common(request, subEnvironment)

def simulate_common(request, postSubEnv=None):
    user = request.user

    solutionFilter = { }
    if not user.is_superuser:
        solutionFilter['envUser__user'] = user
    solutions = Solution.objects.filter(**solutionFilter)

    subenvs = set(solution.subEnvironment for solution in solutions)
    allTests = [ ]
    for subenv in subenvs:
        subEnvId = subenv.id
        OfflineTestFormSet = make_offline_test_formset(subEnvId, extra=0)
        if request.method == 'POST' and subenv == postSubEnv:
            formset = OfflineTestFormSet(request.POST, request.FILES)
            if formset.is_valid():
                return handle_offline_test_formset(formset)
        else:
            formset = OfflineTestFormSet()
        allTests.append({
            'subEnvironment': subenv,
            'formset': formset,
        })

    def niceValue(field):
        value = field.value()
        if field.name == 'solution':
            solution = solutions.get(pk=value)
            return os.path.basename(solution.file.name)
        else:
            return value

    for test in allTests:
        formset = test['formset']
        aaData = [ [ (niceValue(field), field.errors) for field in form ]
            for form in formset ]
        cellIds = [ [ field.id_for_label for field in form ] for form in formset ]
        rowIds = [ form['id'].value() for form in formset ]
        test['aaData'] = json.dumps(aaData)
        test['cellIds'] = json.dumps(cellIds)
        test['rowIds'] = json.dumps(rowIds)

    unusedFilter = {
        'offlinetest__isnull': True,
    }
    if not user.is_superuser:
        unusedFilter['envUser__user'] = user
    unusedQueryset = Solution.objects.filter(**unusedFilter)
    RadioOfflineTestForm = make_radio_offline_test_form({
        'queryset': unusedQueryset,
    })

    return render_to_response('simulator/simulate.html',
        {
            'form': RadioOfflineTestForm(),
            'allTests': allTests,
        },
        context_instance = RequestContext(request))

def handle_offline_test_formset(formset):
    formset.save()
    return HttpResponseRedirect(reverse(simulate))

def make_base_solution_formset(queryset):
    class BaseScheduleFormSet(make_base_custom_formset(queryset)):
        def clean(self):
            if any(self.errors):
                return
            for form in self.forms:
                cleaned_data = form.cleaned_data
                print cleaned_data
            super(BaseScheduleFormSet, self).clean()

    return BaseScheduleFormSet

def schedule(request):
    user = request.user

    solutionFilter = { }
    if not user.is_superuser:
        solutionFilter['envUser__user'] = user
    solutions = Solution.objects.filter(**solutionFilter)

    schedules = Schedule.objects.filter(solution__in=solutions)
    ScheduleFormSet = modelformset_factory(Schedule, can_delete=True,
        formset=make_base_solution_formset(schedules))
    if request.method == 'POST':
        formset = ScheduleFormSet(request.POST)
        if formset.is_valid():
            formset.save()
            return HttpResponseRedirect(reverse(schedule))
    else:
        formset = ScheduleFormSet()

    return render_to_response('simulator/schedule.html',
        {
            'formset': formset,
        },
        context_instance = RequestContext(request))

@login_required
def add_new_test(request):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        unusedFilter = {
            'offlinetest__isnull': True,
        }
        if not user.is_superuser:
            unusedFilter['envUser__user'] = user
        unusedQueryset = Solution.objects.filter(**unusedFilter)
        RadioOfflineTestForm = make_radio_offline_test_form({
            'queryset': unusedQueryset,
        })
        form = RadioOfflineTestForm(request.POST, request.FILES)
        if form.is_valid():
            test = form.save()
            response = test.id

    return HttpResponse(response, mimetype="text/plain")

@login_required
def delete_test(request):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        params = request.POST
        testFilter = {
            'id': params[u'id'],
        }
        if not user.is_superuser:
            testFilter['solution__envUser__user'] = user
        test = get_object_or_404(OfflineTest, **testFilter)
        test.delete()
        response = 'ok'

    return HttpResponse(response, mimetype="text/plain")

@login_required
def get_other_solutions(request):
    params = request.GET
    user = request.user

    if u'subEnvId' in params:
        subEnvId = params[u'subEnvId']
        unusedFilter = {
            'offlinetest__isnull': True,
            'subEnvironment': subEnvId,
        }
        if not user.is_superuser:
            unusedFilter['envUser__user'] = user
        unusedQueryset = Solution.objects.filter(**unusedFilter)
        RadioOfflineTestForm = make_radio_offline_test_form({
            'queryset': unusedQueryset,
        })
        jsonObj = RadioOfflineTestForm().as_table()
    else:
        jsonObj = ''

    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")

@login_required
def run_simulation(request):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        subEnvId = request.POST[u'subEnvId']
        subEnvironment = get_object_or_404(SubEnvironment, id=subEnvId)
        tests = queryOfflineTests(user, subEnvironment)
        process = getSandboxProcess(subEnvironment, tests)
        process.start()
        return HttpResponseRedirect(reverse(simulate))

    return HttpResponse(response, mimetype="text/plain")

def queryOfflineTests(user, subEnvironment):
    offlineTestFilter = { }
    if not user.is_superuser:
        offlineTestFilter['solution__envUser__user'] = user
    if subEnvironment:
        offlineTestFilter['solution__subEnvironment'] = subEnvironment
    offlineTests = OfflineTest.objects.filter(**offlineTestFilter)
    return offlineTests
