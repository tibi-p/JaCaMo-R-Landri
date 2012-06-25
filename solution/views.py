from django import forms
from django.contrib.auth.decorators import login_required
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvUser
from home.base import make_base_custom_formset
from solution.models import Solution
from subenvironment.models import SubEnvironment

def make_solution_form(subEnvKwArgs={ }):
    hidden = subEnvKwArgs.pop('hidden', False)
    if hidden:
        class SolutionForm(forms.models.ModelForm):
            class Meta:
                model = Solution
                exclude = ('envUser', 'subEnvironment')
    else:
        if subEnvKwArgs.get('queryset', None) is None:
            subEnvKwArgs['queryset'] = SubEnvironment.objects.all()

        class SolutionForm(forms.models.ModelForm):
            subEnvironment = forms.ModelChoiceField(**subEnvKwArgs)

            class Meta:
                model = Solution
                exclude = ('envUser',)

    return SolutionForm

def make_solution_formset(userSolutions, subenvs, singleSubEnv=False):
    subEnvKwArgs = { }
    if singleSubEnv:
        subEnvKwArgs['hidden'] = True
    else:
        subEnvKwArgs['queryset'] = subenvs

    SolutionForm = make_solution_form(subEnvKwArgs)
    solutions = userSolutions.filter(subEnvironment__in=subenvs)
    BaseSolutionFormSet = make_base_custom_formset(solutions)
    return modelformset_factory(Solution, form=SolutionForm,
        formset=BaseSolutionFormSet)

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
        '''
        SolutionFormSet = make_solution_formset(userSolutions,
            SubEnvironment.objects.filter(pk=subEnvironment.pk), singleSubEnv=True)
        if request.method == 'POST' and subEnvironment == postSubEnv:
            formset = SolutionFormSet(request.POST, request.FILES)
            if formset.is_valid():
                return handle_solution_formset(formset, envUser, subEnvironment)
        else:
            formset = SolutionFormSet()
        '''
        
        subenvs = SubEnvironment.objects.filter(pk=subEnvironment.pk)
        
        print "subenvs:",subenvs
        
        SolutionForm = make_solution_form({
                                           'hidden':True,
                                           'queryset':subenvs,
                                           })
        
        solutions = userSolutions.filter(subEnvironment__in=subenvs)
        
        forms = []
        
        print "solutions: ",solutions
        
        for solution in solutions:
            forms.append(SolutionForm(instance=solution))
            
        forms.append(SolutionForm())
        
        allSolutions.append({
            'subEnvironment': subEnvironment,
            'forms': forms,
        })
        
        print "forms: ",forms

    SolutionFormSet = make_solution_formset(userSolutions,
        unsubEnvironments)
    if request.method == 'POST' and others:
        print request.POST, request.FILES
        othersFormset = SolutionFormSet(request.POST, request.FILES)
        if othersFormset.is_valid():
            return handle_solution_formset(othersFormset, envUser)
    else:
        othersFormset = SolutionFormSet()

    return render_to_response('solution/index.html',
        {
            'allSolutions': allSolutions,
            'othersFormset': othersFormset,
        },
        context_instance = RequestContext(request))

def handle_solution_formset(formset, envUser, subEnvironment=None):
    solutions = formset.save(commit=False)
    for soln in solutions:
        soln.envUser = envUser
        if subEnvironment is not None:
            soln.subEnvironment = subEnvironment
        soln.save()
    return HttpResponseRedirect(reverse(index))
