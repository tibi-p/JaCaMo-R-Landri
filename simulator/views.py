from django import forms
from django.contrib.auth.decorators import login_required
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from home.base import make_base_custom_formset
from schedule.models import Schedule
from simulator.models import AbstractProcess
from simulator.turn import runTurn
from solution.models import Solution
from subenvironment.models import SubEnvironment
from collections import defaultdict

@login_required
def run(request):
    runTurn(10)
    subEnvironmentList = SubEnvironment.objects.all()
    return render_to_response('simulator/run.html',
        { },
        context_instance=RequestContext(request))

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

@login_required
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
def run_simulation(request, section):
    params = request.POST
    user = request.user
    response = 'failure'

    if request.method == 'POST' and u'solution' in params:
        solutionFilter = {
            'id': request.POST[u'solution'],
        }
        if not user.is_superuser:
            solutionFilter['envUser__user'] = user
        solution = get_object_or_404(Solution, **solutionFilter)
        
        # TODO
        AbstractProcess.objects.all().delete()
        abstractProcess = AbstractProcess(solution=solution)
        abstractProcess.save()
        # TODO remove me ? change logic for IE ?
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
        simulate_view = [ section, 'views', 'simulate_process' ]
        simulate_view = '.'.join(simulate_view)
        url = reverse(simulate_view, args=[ abstractProcess.id ])
        return HttpResponseRedirect(url)

    return HttpResponse(response, mimetype="text/plain")
