from django import forms
from django.contrib.auth.decorators import login_required
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvUser
from home.base import make_base_custom_formset
from schedule.models import OfflineTest, Schedule
from simulator.models import AbstractProcess
from simulator.turn import getSandboxProcess, runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
from collections import defaultdict
import json
import os

class OfflineTestForm(forms.models.ModelForm):
    class Meta:
        model = OfflineTest

def make_custom_offline_test_form(testKwArgs={ }):
    queryset = testKwArgs.get('queryset', None)
    if queryset is None:
        queryset = Solution.objects.all()
        testKwArgs['queryset'] = queryset
    if testKwArgs.get('initial', None) is None:
        if queryset:
            testKwArgs['initial'] = queryset[0]

    class CustomOfflineTestForm(OfflineTestForm):
        solution = forms.models.ModelChoiceField(**testKwArgs)

    return CustomOfflineTestForm

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

@login_required
def run(request):
    runTurn(5)
    subEnvironmentList = SubEnvironment.objects.all()
    return render_to_response('simulator/run.html',
        { },
        context_instance=RequestContext(request))

@login_required
def simulate(request):
    return simulate_common(request)

@login_required
def simulate_post(request, subEnvId):
    subEnvironment = SubEnvironment.objects.get(pk=subEnvId)
    return simulate_common(request, targetEnv=subEnvironment)

@login_required
def simulate_proc(request, procId):
    process = AbstractProcess.objects.get(pk=procId)
    return simulate_common(request, abstractProcess=process)

def simulate_common(request, targetEnv=None, abstractProcess=None):
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
        if request.method == 'POST' and subenv == targetEnv:
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
    CustomOfflineTestForm = make_custom_offline_test_form({
        'queryset': unusedQueryset,
    })

    return render_to_response('simulator/simulate.html',
        {
            'form': CustomOfflineTestForm(),
            'allTests': allTests,
            'abstractProcess': abstractProcess,
        },
        context_instance=RequestContext(request))

def handle_offline_test_formset(formset):
    formset.save()
    return HttpResponseRedirect(reverse(simulate))

def make_schedule_form(solnEnvKwArgs={ }):
    if solnEnvKwArgs.get('queryset', None) is None:
        solnEnvKwArgs['queryset'] = Solution.objects.all()

    class ScheduleForm(forms.models.ModelForm):
        solution = forms.ModelChoiceField(**solnEnvKwArgs)

        class Meta:
            model = Schedule

    return ScheduleForm

def make_base_schedule_formset(queryset):
    BaseCustomFormset = make_base_custom_formset(queryset)

    class BaseScheduleFormSet(BaseCustomFormset):
        def clean(self):
            if any(self.errors):
                return
            schedule = defaultdict(lambda: [ ])
            for form in self.forms:
                cleaned_data = form.cleaned_data
                if cleaned_data and not cleaned_data.get('DELETE', False):
                    step = cleaned_data['step']
                    schedule[step].append(cleaned_data)
            for step, entries in schedule.iteritems():
                print [ entry['solution'] for entry in entries ]
            #print schedule
            super(BaseScheduleFormSet, self).clean()

    return BaseScheduleFormSet

def schedule(request):
    user = request.user

    solutionFilter = { }
    if not user.is_superuser:
        solutionFilter['envUser__user'] = user
    solutions = Solution.objects.filter(**solutionFilter)
    schedules = Schedule.objects.filter(solution__in=solutions)
    schedules = schedules.order_by('step')

    ScheduleForm = make_schedule_form({
        'queryset': solutions,
    })
    BaseScheduleFormset = make_base_schedule_formset(schedules)
    ScheduleFormSet = modelformset_factory(Schedule, can_delete=True,
        form=ScheduleForm, formset=BaseScheduleFormset)

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
        context_instance=RequestContext(request))

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
        CustomOfflineTestForm = make_custom_offline_test_form({
            'queryset': unusedQueryset,
        })
        form = CustomOfflineTestForm(request.POST, request.FILES)
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

    if request.method == 'GET' and u'subEnvId' in params:
        subEnvId = params[u'subEnvId']
        unusedFilter = {
            'offlinetest__isnull': True,
            'subEnvironment': subEnvId,
        }
        if not user.is_superuser:
            unusedFilter['envUser__user'] = user
        unusedQueryset = Solution.objects.filter(**unusedFilter)
        CustomOfflineTestForm = make_custom_offline_test_form({
            'queryset': unusedQueryset,
        })
        jsonObj = CustomOfflineTestForm().as_table()
    else:
        jsonObj = ''

    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")

runningInfo = defaultdict(lambda: { })

@login_required
def run_simulation(request):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        envUser = get_object_or_404(EnvUser, user=user)
        subEnvId = request.POST[u'subEnvId']
        subEnvironment = get_object_or_404(SubEnvironment, id=subEnvId)
        # TODO AbstractProcess.objects.all().delete()
        abstractProcess = AbstractProcess(envUser=envUser, subEnvironment=subEnvironment)
        abstractProcess.save()
        '''
        tests = queryOfflineTests(user, subEnvironment)
        process, pipes = getSandboxProcess(subEnvironment, tests)
        process.start()
        pipes[1].close()
        while True:
            print "(*-*) let's"
            try:
                msg = pipes[0].recv()
                print '#### ruins: ' + msg
            except EOFError:
                break
        #return HttpResponse("ok", mimetype="application/json")
        '''
        url = reverse(simulate_proc, args=[ abstractProcess.id ])
        return HttpResponseRedirect(url)

    return HttpResponse(response, mimetype="text/plain")

def queryOfflineTests(user, subEnvironment):
    offlineTestFilter = { }
    if not user.is_superuser:
        offlineTestFilter['solution__envUser__user'] = user
    if subEnvironment:
        offlineTestFilter['solution__subEnvironment'] = subEnvironment
    offlineTests = OfflineTest.objects.filter(**offlineTestFilter)
    return offlineTests
