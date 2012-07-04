from django import forms
from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from simulator.models import AbstractProcess
from solution.models import Solution
from subenvironment.models import SubEnvironment

def make_solution_selector_form(solutionArgs={ }):
    # TODO use not in instead... check other places too
    if solutionArgs.get('queryset', None) is None:
        solutionArgs['queryset'] = Solution.objects.all()
    # TODO fix me using is None
    solutionArgs['empty_label'] = None
    
    if len(solutionArgs['queryset']) == 0:
        return None

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
    subEnvironment = get_object_or_404(SubEnvironment, pk=subEnvironmentId)
    return detail_common(request, subEnvironment=subEnvironment)

@login_required
def simulate_process(request, processId):
    process = get_object_or_404(AbstractProcess, pk=processId)
    return detail_common(request, abstractProcess=process)

def detail_common(request, subEnvironment=None, abstractProcess=None):
    user = request.user
    
    if not subEnvironment and abstractProcess:
        subEnvironment = abstractProcess.solution.subEnvironment

    solutionFilter = { 'subEnvironment': subEnvironment }
    if not user.is_superuser:
        solutionFilter['envUser__user'] = user
    SolutionSelectorForm = make_solution_selector_form({
        'queryset': Solution.objects.filter(**solutionFilter),
    });
    if SolutionSelectorForm:
        solutionForm = SolutionSelectorForm()
    else:
        solutionForm = None
    return render_to_response('subenvironment/detail.html',
           {
               'subEnvironment': subEnvironment,
               'form': solutionForm,
               'abstractProcess': abstractProcess,
           },
           context_instance=RequestContext(request))
