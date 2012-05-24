from django import forms
from django.forms.models import modelformset_factory
from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvUser
from schedule.models import OfflineTest, Schedule
from simulator.turn import getSandboxProcess, runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
import json

def make_solution_form(subEnvKwArgs={ }):
    if 'queryset' not in subEnvKwArgs or subEnvKwArgs['queryset'] is None:
        subEnvKwArgs['queryset'] = SubEnvironment.objects.all()
    if 'widget' not in subEnvKwArgs or subEnvKwArgs['widget'] is None:
        subEnvKwArgs['widget'] = forms.Select()

    class SolutionForm(forms.models.ModelForm):
        subEnvironment = forms.ModelChoiceField(**subEnvKwArgs)

        def __init__(self, *args, **kwargs):
            super(SolutionForm, self).__init__(*args, **kwargs)

        class Meta:
            model = Solution
            exclude = ('envUser',)

    return SolutionForm

def make_base_custom_formset(queryset):
    class BaseCustomFormSet(forms.models.BaseModelFormSet):
        def __init__(self, *args, **kwargs):
            if queryset is not None and not 'queryset' in kwargs:
                kwargs['queryset'] = queryset
            super(BaseCustomFormSet, self).__init__(*args, **kwargs)

    return BaseCustomFormSet

def make_solution_formset(userSolutions, subenvs, subEnvWidget=None):
    subEnvKwArgs = {
        'queryset': subenvs,
        'widget': subEnvWidget,
    }
    for kwArgs in [ subEnvKwArgs ]:
        queryset = kwArgs['queryset']
        if queryset:
            kwArgs['initial'] = queryset[0]

    SolutionForm = make_solution_form(subEnvKwArgs)
    solutions = userSolutions.filter(subEnvironment__in=subenvs)
    BaseSolutionFormSet = make_base_custom_formset(solutions)
    return modelformset_factory(Solution, form=SolutionForm,
        formset=BaseSolutionFormSet, can_delete=True)

@login_required
def index(request):
    return index_common(request)

@login_required
def index_post(request, subEnvId):
    try:
        subEnvironment = SubEnvironment.objects.get(pk=subEnvId)
        return index_common(request, postSubEnv=subEnvironment)
    except SubEnvironment.DoesNotExist:
        return index_common(request, others=True)

def index_common(request, postSubEnv=None, others=False):
    user = request.user
    envUser = get_object_or_404(EnvUser, user=user)

    userSolutions = Solution.objects.filter(envUser=envUser)

    subEnvironments = SubEnvironment.objects.get_solved_by_user(user)
    unsubEnvironments = SubEnvironment.objects.get_unsolved_by_user(user)
    allSolutions = [ ]
    for subEnvironment in subEnvironments:
        SolutionFormSet = make_solution_formset(userSolutions,
            SubEnvironment.objects.filter(pk=subEnvironment.pk), subEnvWidget=forms.HiddenInput())
        if request.method == 'POST' and subEnvironment == postSubEnv:
            print request.POST, request.FILES
            formset = SolutionFormSet(request.POST, request.FILES)
            if formset.is_valid():
                cleaned_data = formset.cleaned_data
                tempSolutions = formset.save(commit=False)
                for tempSol in tempSolutions:
                    tempSol.envUser = envUser
                    tempSol.save()
                print cleaned_data
                return HttpResponseRedirect('/solution/index/')
        else:
            formset = SolutionFormSet()
        allSolutions.append({
            'subEnvironment': subEnvironment,
            'formset': formset,
        })

    SolutionFormSet = make_solution_formset(userSolutions,
        unsubEnvironments)
    if request.method == 'POST' and others:
        print request.POST, request.FILES
        othersFormset = SolutionFormSet(request.POST, request.FILES)
        if othersFormset.is_valid():
            cleaned_data = othersFormset.cleaned_data
            othersFormset.save()
            print cleaned_data
            return HttpResponseRedirect('/solution/index/')
    else:
        othersFormset = SolutionFormSet()

    return render_to_response('solution/index.html',
        {
            'allSolutions': allSolutions,
            'othersFormset': othersFormset,
        },
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
