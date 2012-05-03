from django import forms
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from schedule.models import Schedule
from solution.models import Solution

class SimulateForm(forms.Form):
    solution = forms.ModelChoiceField(queryset=Solution.objects.none(),
        required=False, empty_label='All solutions')

    def __init__(self, *args, **kwargs):
        currentUser = kwargs.pop('user', None)
        super(SimulateForm, self).__init__(*args, **kwargs)
        if currentUser != None:
            if currentUser.is_superuser:
                queryset = Solution.objects.all()
            else:
                queryset = Solution.objects.filter(envUser__user__id=currentUser.id)
            self.fields['solution'] = forms.ModelChoiceField(queryset=queryset,
                required=False, empty_label='All solutions')

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
