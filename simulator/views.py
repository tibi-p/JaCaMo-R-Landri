from django import forms
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
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

def make_base_custom_formset(queryset):
    class BaseCustomFormSet(forms.models.BaseModelFormSet):
        def __init__(self, *args, **kwargs):
            if queryset is not None and not 'queryset' in kwargs:
                kwargs['queryset'] = queryset
            super(BaseCustomFormSet, self).__init__(*args, **kwargs)

    return BaseCustomFormSet

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
        { 'subEnvironmentList': subEnvironmentList },
        context_instance = RequestContext(request))

@login_required
def simulate(request):
    '''if request.method == 'POST':
        form = SimulateForm(request.POST, user=request.user)
        if form.is_valid():
            cleaned_data = form.cleaned_data
            subenvironment = cleaned_data['subenvironment']
            offlineTests = queryOfflineTests(request.user, subenvironment.id)

            process = getSandboxProcess(subenvironment, offlineTests)
            process.start()
            return HttpResponseRedirect('/simulator/simulate/')
    else:
        form = SimulateForm(user=request.user)'''

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

    subenvs = SubEnvironment.objects.filter(solution__in=solutions).distinct()
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

@login_required
def getsolutions(request, subEnvId):
    offlineTests = queryOfflineTests(request.user, subEnvId)
    solutions = offlineTests.get_solutions()
    jsonObj = { }
    jsonObj['offlineTests'] = serializers.serialize("json", offlineTests)
    jsonObj['solutions'] = serializers.serialize("json", solutions)
    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")

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

def queryOfflineTests(user, subEnvId):
    offlineTestFilter = { }
    if not user.is_superuser:
        offlineTestFilter['solution__envUser__user'] = user
    if subEnvId:
        offlineTestFilter['solution__subEnvironment__id'] = subEnvId
    offlineTests = OfflineTest.objects.filter(**offlineTestFilter)
    return offlineTests

@login_required
def jUpdateOfflineTests(request):
    post = request.POST
    try:
        id = post[u'id']
        value = int(post[u'value'])
        if value > 0:
            test = OfflineTest.objects.get(solution=id)
            test.numAgents = value
            test.save()
    except:
        pass
    return HttpResponse("ok", mimetype="text/plain")
