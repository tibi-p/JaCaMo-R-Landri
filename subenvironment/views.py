from django import forms
from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from solution.models import Solution
from subenvironment.models import SubEnvironment

def make_solution_selector_form(solutionArgs={ }):
    if solutionArgs.get('queryset', None) is None:
        solutionArgs['queryset'] = Solution.objects.all()

    class SolutionSelectorForm(forms.Form):
        solution = forms.ModelChoiceField(**solutionArgs)

    return SolutionSelectorForm

@login_required
def index(request):
    subEnvironmentList = SubEnvironment.objects.all()
    return render_to_response('subenvironment/index.html',
        { 'subEnvironmentList': subEnvironmentList },
        context_instance=RequestContext(request))

@login_required
def detail(request, subEnvironmentId):
    user = request.user

    subEnvironment = get_object_or_404(SubEnvironment, pk=subEnvironmentId)
    solutionFilter = { 'subEnvironment': subEnvironmentId }
    if not user.is_superuser:
        solutionFilter['envUser__user'] = user
    SolutionSelectorForm = make_solution_selector_form({
        'queryset': Solution.objects.filter(**solutionFilter),
    });

    return render_to_response('subenvironment/detail.html',
        {
            'subEnvironment': subEnvironment,
            'form': SolutionSelectorForm(),
        },
        context_instance=RequestContext(request))
