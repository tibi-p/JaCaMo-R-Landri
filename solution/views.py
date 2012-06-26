from django import forms
from django.contrib.auth.decorators import login_required
from django.conf import settings
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvAgent, EnvUser
from home.base import fill_object, make_base_custom_formset
from solution.models import Solution, solution_upload_to
from subenvironment.models import SubEnvironment
from specification import SolutionSpecification
from zipfile import ZipFile
import json
import os


def get_agent_code_from_zip(filename):
    
    print filename
    with ZipFile(filename, 'r') as zipFile:
        return zipFile.namelist()
    

def tweak_keywords(keywords, attr, defaults):
    queryset = keywords.get(attr, None)
    if queryset is None:
        queryset = defaults[attr]
        keywords[attr] = queryset
    if keywords.get('initial', None) is None:
        if queryset:
            keywords['initial'] = queryset[0]

def make_custom_agent_form(agent_kwargs={ }, asl_kwargs={ }):
    tweak_keywords(agent_kwargs, 'queryset', {
        'queryset': EnvAgent.objects.none(),
    })
    tweak_keywords(asl_kwargs, 'choices', {
        'choices': [ ],
    })

    class AgentForm(forms.Form):
        agent_name = forms.models.ModelChoiceField(**agent_kwargs)
        asl = forms.ChoiceField(**asl_kwargs)
        cardinality = forms.IntegerField(min_value=1)

    return AgentForm

def make_solution_form(subEnvKwArgs={ }):
    description_widget = forms.Textarea({
        'rows': 5,
    })
    hidden = subEnvKwArgs.pop('hidden', False)
    if hidden:
        class SolutionForm(forms.models.ModelForm):
            description = forms.CharField(widget=description_widget)

            class Meta:
                model = Solution
                exclude = ('envUser', 'subEnvironment')
    else:
        if subEnvKwArgs.get('queryset', None) is None:
            subEnvKwArgs['queryset'] = SubEnvironment.objects.all()

        class SolutionForm(forms.models.ModelForm):
            subEnvironment = forms.ModelChoiceField(**subEnvKwArgs)
            description = forms.CharField(widget=description_widget)

            class Meta:
                model = Solution
                exclude = ('envUser',)

    return SolutionForm

def make_special_solution_form(userSolutions, subenvs, singleSubEnv=False):
    subEnvKwArgs = { }
    if singleSubEnv:
        subEnvKwArgs['hidden'] = True
    else:
        subEnvKwArgs['queryset'] = subenvs

    return make_solution_form(subEnvKwArgs)

def make_solution_formset(userSolutions, subenvs, singleSubEnv=False):
    SolutionForm = make_special_solution_form(userSolutions, subenvs,
        singleSubEnv)
    solutions = userSolutions.filter(subEnvironment__in=subenvs)
    BaseSolutionFormSet = make_base_custom_formset(solutions)
    return modelformset_factory(Solution, form=SolutionForm,
        formset=BaseSolutionFormSet)

def make_single_solution_formset(SolutionForm, solution, **kwargs):
    solutions = Solution.objects.filter(pk=solution.pk)
    BaseSolutionFormSet = make_base_custom_formset(solutions)
    return modelformset_factory(Solution, form=SolutionForm,
        formset=BaseSolutionFormSet, **kwargs)

@login_required
def index(request):
    return index_common(request)

@login_required
def index_change(request, solutionId):
    solution = get_object_or_404(Solution, pk=solutionId)
    return index_common(request, postSolution=solution)

@login_required
def index_add(request, subEnvId):
    try:
        subEnvironment = SubEnvironment.objects.get(pk=subEnvId)
        return index_common(request, postSubEnv=subEnvironment)
    except SubEnvironment.DoesNotExist:
        return index_common(request, others=True)

@login_required
def index_remove(request, solutionId):
    solution = get_object_or_404(Solution, pk=solutionId)
    solution.delete()
    return index_common(request)

def index_common(request, postSolution=None, postSubEnv=None, others=False):
    is_post = (request.method == 'POST')
    user = request.user
    envUser = get_object_or_404(EnvUser, user=user)

    userSolutions = Solution.objects.filter(envUser=envUser)
    subEnvironments = SubEnvironment.objects.get_solved_by_user(user)
    unsubEnvironments = SubEnvironment.objects.get_unsolved_by_user(user)

    allSolutions = [ ]
    for subEnvironment in subEnvironments:
        subenvs = SubEnvironment.objects.filter(pk=subEnvironment.pk)
        SolutionForm = make_special_solution_form(userSolutions, subenvs,
            singleSubEnv=True)
        solutions = userSolutions.filter(subEnvironment__in=subenvs)

        forms = [ ]
        for solution in solutions:
            SolutionFormSet = make_single_solution_formset(SolutionForm,
                solution, extra=0)
            if is_post and solution == postSolution:
                formset = SolutionFormSet(request.POST, request.FILES)
                if formset.is_valid():
                    return handle_solution_formset(formset, {
                        'envUser': envUser,
                        'subEnvironment': subEnvironment,
                    })
            else:
                formset = SolutionFormSet()
            forms.append({
                'form': formset,
                'obj': solution,
                'is_novel': False,
            })

        if is_post and subEnvironment == postSubEnv:
            form = SolutionForm(request.POST, request.FILES)
            if form.is_valid():
                return handle_solution_form(form, {
                    'envUser': envUser,
                    'subEnvironment': subEnvironment,
                })
        else:
            form = SolutionForm()
        forms.append({
            'form': form,
            'obj': subEnvironment,
            'is_novel': True,
        })

        allSolutions.append({
            'subEnvironment': subEnvironment,
            'forms': forms,
        })

    SolutionFormSet = make_solution_formset(userSolutions,
        unsubEnvironments)
    if is_post and others:
        othersFormset = SolutionFormSet(request.POST, request.FILES)
        if othersFormset.is_valid():
            return handle_solution_formset(othersFormset, {
                'envUser': envUser,
            })
    else:
        othersFormset = SolutionFormSet()

    AgentForm = make_custom_agent_form()
    return render_to_response('solution/index.html',
        {
            'allSolutions': allSolutions,
            'othersFormset': othersFormset,
            'agentForm': AgentForm(),
        },
        context_instance=RequestContext(request))

def handle_solution_formset(form, attributes):
    solutions = form.save(commit=False)
    for solution in solutions:
        fill_object(solution, attributes)
    return HttpResponseRedirect(reverse(index))

def handle_solution_form(form, attributes):
    solution = form.save(commit=False)
    fill_object(solution, attributes)
    
    xml = SolutionSpecification.make_xml([], str(solution.artifacts), str(solution.organizations))
    
    filename = solution_upload_to(solution, "config_" + str(solution.id) + ".xml")
    with open(os.path.join(settings.MEDIA_ROOT, filename), "w") as xmlConfigFile:
        xmlConfigFile.write(xml.toprettyxml())
    
    return HttpResponseRedirect(reverse(index))

@login_required
def add_agent(request):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        unusedFilter = {
        }
        if not user.is_superuser:
            unusedFilter['envUser__user'] = user
        unusedQueryset = Solution.objects.filter(**unusedFilter)
        AgentForm = make_custom_agent_form({
            'queryset': unusedQueryset,
        })
        form = AgentForm(request.POST, request.FILES)
        if form.is_valid():
            cleaned_data = form.cleaned_data
            # TODO add stuff here
            print cleaned_data

    return HttpResponse(response, mimetype="text/plain")

@login_required
def delete_agent(request):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        params = request.POST
        agentFilter = {
            'id': params[u'id'],
        }
        if not user.is_superuser:
            agentFilter['envUser__user'] = user
        envAgent = get_object_or_404(EnvAgent, **agentFilter)
        # TODO delete stuff here
        response = 'ok'

    return HttpResponse(response, mimetype="text/plain")

@login_required
def get_other_agents(request):
    params = request.GET
    user = request.user

    if request.method == 'GET' and u'solutionId' in params:
        solutionId = params[u'solutionId']
        solution = get_object_or_404(Solution, pk=solutionId)
        unusedFilter = {
        }
        if not user.is_superuser:
            unusedFilter['envUser__user'] = user
        unusedQueryset = EnvAgent.objects.filter(**unusedFilter)
        
        agentFiles= get_agent_code_from_zip(solution.agents.file.name)
        choices = [(fileName,fileName) for fileName in agentFiles]
        # TODO create choices here
        # INSPECT solution.agents 
        AgentForm = make_custom_agent_form({
            'queryset': unusedQueryset,
        }, {
            'choices': choices,
        })
        jsonObj = AgentForm().as_table()
    else:
        jsonObj = ''

    jsonStr = json.dumps(jsonObj)
    return HttpResponse(jsonStr, mimetype="application/json")
