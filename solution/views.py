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

def make_solution_form(userQueryset):
    class SolutionForm(forms.models.ModelForm):
        envUser = forms.ModelChoiceField(queryset=EnvUser.objects.all(),
            widget=forms.HiddenInput())

        def __init__(self, *args, **kwargs):
            super(SolutionForm, self).__init__(*args, **kwargs)
            if userQueryset != None:
                self.fields['envUser'].queryset = userQueryset

        class Meta:
            model = Solution

    return SolutionForm

def make_base_custom_formset(queryset):
    class BaseCustomFormSet(forms.models.BaseModelFormSet):
        def __init__(self, *args, **kwargs):
            if queryset is not None and not 'queryset' in kwargs:
                kwargs['queryset'] = queryset
            super(BaseCustomFormSet, self).__init__(*args, **kwargs)

    return BaseCustomFormSet

@login_required
def index(request):
    user = request.user
    envUser = get_object_or_404(EnvUser, user=user)
    subEnvironments = SubEnvironment.objects.get_solved_by_user(user)
    solutions = Solution.objects
    allSolutions = [ ]
    for subEnvironment in subEnvironments:
        allSolutions.append({
            'subEnvironment': subEnvironment,
            'someSolutions': solutions.get_sent_by_user_for_env(user, subEnvironment),
        })

    envUser = get_object_or_404(EnvUser, user=request.user)
    userQueryset = EnvUser.objects.filter(pk=envUser.pk)
    customQueryset = Solution.objects.filter(envUser=envUser)
    formArgs = {
        'initial': {
            'envUser': envUser,
        },
    }

    SolutionForm = make_solution_form(userQueryset)
    BaseSolutionFormSet = make_base_custom_formset(customQueryset)
    SolutionFormSet = modelformset_factory(Solution, form=SolutionForm,
        formset=BaseSolutionFormSet, can_delete=True)
    if request.method == 'POST':
        formset = SolutionFormSet(request.POST, request.FILES)
        if formset.is_valid():
            cleaned_data = formset.cleaned_data
            formset.save()
            print cleaned_data
            return HttpResponseRedirect('/solution/index/')
    else:
        formset = SolutionFormSet()

    return render_to_response('solution/index.html',
        {
            'formset': formset,
            'allSolutions': allSolutions,
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
