from django import forms
from django.conf import settings
from django.contrib.auth.decorators import login_required
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
from validate import Validator
import json
import os
from zipfile import BadZipfile, ZipFile

def get_config_filename(solution):
    basename = 'config_%s.xml' % (solution.id,)
    return solution_upload_to(solution, basename)

def get_config_filepath(solution):
    filename = get_config_filename(solution)
    return os.path.join(settings.MEDIA_ROOT, filename)

def get_agent_code_from_zip(filename):
    
    print filename
    try:
        with ZipFile(filename, 'r') as zipFile:
            return zipFile.namelist()
    except BadZipfile:
        return []
        

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

def make_solution_form(envUser, subEnvKwArgs={ }):
    description_widget = forms.Textarea({
        'rows': 5,
    })
    hidden = subEnvKwArgs.pop('hidden', False)
    if hidden:
        class SolutionForm(forms.models.ModelForm):
            description = forms.CharField(widget=description_widget)

            def clean_artifacts(self):
                artifacts = self.cleaned_data['artifacts']
                userID = envUser.id
                if not Validator.validateSolution(artifacts, userID):
                    raise forms.ValidationError("Should upload a valid .jar with artifacts")
                return artifacts

            class Meta:
                model = Solution
                exclude = ('envUser', 'subEnvironment')
    else:
        if subEnvKwArgs.get('queryset', None) is None:
            subEnvKwArgs['queryset'] = SubEnvironment.objects.all()

        class SolutionForm(forms.models.ModelForm):
            subEnvironment = forms.ModelChoiceField(**subEnvKwArgs)
            description = forms.CharField(widget=description_widget)

            def clean_artifacts(self):
                artifacts = self.cleaned_data['artifacts']
                userID = envUser.id
                if not Validator.validateSolution(artifacts, userID):
                    raise forms.ValidationError("Should upload a valid .jar with artifacts")
                return artifacts

            class Meta:
                model = Solution
                exclude = ('envUser',)

    return SolutionForm

def make_special_solution_form(envUser, subenvs, singleSubEnv=False):
    subEnvKwArgs = { }
    if singleSubEnv:
        subEnvKwArgs['hidden'] = True
    else:
        subEnvKwArgs['queryset'] = subenvs

    return make_solution_form(envUser, subEnvKwArgs)

def make_solution_formset(envUser, userSolutions, subenvs, singleSubEnv=False):
    SolutionForm = make_special_solution_form(envUser, subenvs,
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

    agentFilter = { }
    if not user.is_superuser:
        agentFilter['envUser__user'] = user
    agentQueryset = EnvAgent.objects.filter(**agentFilter)

    allSolutions = [ ]
    for subEnvironment in subEnvironments:
        subenvs = SubEnvironment.objects.filter(pk=subEnvironment.pk)
        SolutionForm = make_special_solution_form(envUser, subenvs,
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
                
            ##FIXME: Make sure this error is treated correctly
            try:
                agentFiles = get_agent_code_from_zip(solution.agents.file.name)
            except IOError:
                agentFiles = []
                
            choices = [ (filename, filename) for filename in agentFiles ]
            AgentForm = make_custom_agent_form({
                'queryset': agentQueryset,
            }, {
                'choices': choices,
            })
            forms.append({
                'form': formset,
                'agent_form': AgentForm(),
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

    SolutionFormSet = make_solution_formset(envUser, userSolutions,
        unsubEnvironments)
    if is_post and others:
        othersFormset = SolutionFormSet(request.POST, request.FILES)
        if othersFormset.is_valid():
            return handle_solution_formset(othersFormset, {
                'envUser': envUser,
            })
    else:
        othersFormset = SolutionFormSet()

    AgentForm = make_custom_agent_form({
        'queryset': agentQueryset,
    }, {
        'choices': [ ],
    })
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
    
    xml = SolutionSpecification.make_xml(str(solution.artifacts), str(solution.organizations))
    config = get_config_filepath(solution)
    with open(config, "w") as xmlConfigFile:
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
def change_agent_mapping(request, solutionId):
    user = request.user
    response = 'failure'

    if request.method == 'POST':
        params = request.POST
        if u'json' in params:
            try:
                solution = get_solution_or_404(user, id=solutionId)
                config = get_config_filepath(solution)
                agents = json.loads(params[u'json'])
                
                print "AGENTS",agents
                Validator.validateAgentMapping(agents)
                
                print config
                with file(config) as f:
                    print f.read()
                # TODO server-side validation goes here
                dom = SolutionSpecification.add_agents_to_xml(config, agents)
                
                print dom.toprettyxml()
                
                with open(config, "w") as f:
                    f.write(dom.toprettyxml())
                response = 'ok'
            except ValueError, e:
                response = str(e)

    return HttpResponse(response, mimetype="text/plain")

def get_solution_or_404(user, **kwargs):
    if not user.is_superuser:
        kwargs['envUser__user'] = user
    return get_object_or_404(Solution, **kwargs)
