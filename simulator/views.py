from django import forms
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext
from schedule.models import OfflineTest, Schedule
from simulator.turn import getSandboxProcess, runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
import json

class OfflineTestForm(forms.models.ModelForm):
    solution = forms.models.ModelChoiceField(Solution.objects.all(), widget=forms.TextInput(attrs={'class':'disabled', 'readonly':'readonly'}))

    class Meta:
        model = OfflineTest

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
    return modelformset_factory(OfflineTest, form=OfflineTestForm,
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
    print subEnvId
    subEnvironment = SubEnvironment.objects.get(pk=subEnvId)
    return simulate_common(request, subEnvironment)

def simulate_common(request, postSubEnv=None):
    user = request.user
    subEnvFilter = { 'solution__offlinetest__isnull': False }
    if not user.is_superuser:
        subEnvFilter['solution__envUser__user__id'] = user.id
    subenvs = SubEnvironment.objects.filter(**subEnvFilter).distinct()
    allTests = [ ]
    for subenv in subenvs:
        subEnvId = subenv.id
        OfflineTestFormSet = make_offline_test_formset(subEnvId, extra=0)
        print request.method, subEnvId, postSubEnv, subEnvId == postSubEnv
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

    for test in allTests:
        formset = test['formset']
        aaData = [ [ field.value() for field in form ] for form in formset ]
        errors = [ [ field.errors for field in form ] for form in formset ]
        cellIds = [ [ field.id_for_label for field in form ] for form in formset ]
        rowIds = [ form['solution'].value() for form in formset ]
        test['aaData'] = json.dumps(aaData)
        test['errors'] = json.dumps(errors)
        test['cellIds'] = json.dumps(cellIds)
        test['rowIds'] = json.dumps(rowIds)
    return render_to_response('simulator/simulate.html',
        {
            #'form': form,
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
