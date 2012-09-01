from django import forms
from django.contrib.auth.decorators import login_required
from django.core.urlresolvers import reverse
from django.forms.models import modelformset_factory
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvAgent, EnvUser
from home.base import fill_object, get_agent_name_list, \
    make_base_custom_formset
from solution.models import Solution
from subenvironment.models import SubEnvironment
from specification import SolutionSpecification
from validate import Validator
import json

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
        agent_class = forms.CharField(required=False)
        cardinality = forms.IntegerField(min_value=1)

    return AgentForm

def validate_artifacts(artifacts, userID):
    if artifacts is not None:
        if not Validator.validateSolution(artifacts, userID):
            error_msg = "Should upload a valid .jar with artifacts"
            raise forms.ValidationError(error_msg)
    return artifacts

def validate_agents(agents, userID):
    if not Validator.validateAgentZip(agents, userID):
        error_msg = "Should upload a valid .zip with agents"
        raise forms.ValidationError(error_msg)
    return agents

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
                return validate_artifacts(artifacts, envUser.id)
            
            def clean_agents(self):
                agents = self.cleaned_data['agents']
                return validate_agents(agents, envUser.id)

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
                return validate_artifacts(artifacts, envUser.id)
            
            def clean_agents(self):
                agents = self.cleaned_data['agents']
                return validate_agents(agents, envUser.id)

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
    solution = get_solution_or_404(request.user, pk=solutionId)
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

    active_subenv = None
    active_solution = None

    allSolutions = [ ]
    for subenv_index, subEnvironment in enumerate(subEnvironments):
        subenvs = SubEnvironment.objects.filter(pk=subEnvironment.pk)
        SolutionForm = make_special_solution_form(envUser, subenvs,
            singleSubEnv=True)
        solutions = userSolutions.filter(subEnvironment__in=subenvs)

        forms = [ ]
        for soln_index, solution in enumerate(solutions):
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
                    active_subenv = {
                        'id': subEnvironment.id,
                        'index': subenv_index,
                    }
                    active_solution = soln_index
            else:
                formset = SolutionFormSet()

            agentFiles = get_agent_name_list(solution.agents.path)
            choices = [ (filename, filename) for filename in agentFiles ]
            AgentForm = make_custom_agent_form({
                'queryset': agentQueryset,
            }, {
                'choices': choices,
            })

            config = solution.get_config_filepath()
            aaData = SolutionSpecification.parseAgentMapping(config)
            rowIds = [ row[0][0] for row in aaData ]

            forms.append({
                'form': formset,
                'agent_form': AgentForm(),
                'obj': solution,
                'is_novel': False,
                'table': {
                    'aaData': json.dumps(aaData),
                    'rowIds': json.dumps(rowIds),
                },
            })

        if is_post and subEnvironment == postSubEnv:
            form = SolutionForm(request.POST, request.FILES)
            if form.is_valid():
                return handle_solution_form(form, {
                    'envUser': envUser,
                    'subEnvironment': subEnvironment,
                })
            else:
                active_subenv = {
                    'id': subEnvironment.id,
                    'index': subenv_index,
                }
                active_solution = len(solutions)
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
            }, are_novel=True)
        else:
            active_subenv = {
                'index': len(subEnvironments),
            }
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
            'active_subenv': active_subenv,
            'active_solution': active_solution,
        },
        context_instance=RequestContext(request))

def handle_solution_formset(form, attributes, are_novel=False):
    solutions = form.save(commit=False)
    for solution in solutions:
        fill_object(solution, attributes)
    if are_novel:
        for solution in solutions:
            # TODO modify to .name & test
            xml = SolutionSpecification.make_xml(solution)
            config = solution.get_config_filepath()
            with open(config, "w") as xmlConfigFile:
                xmlConfigFile.write(xml.toprettyxml())
    return HttpResponseRedirect(reverse(index))

def handle_solution_form(form, attributes):
    solution = form.save(commit=False)
    fill_object(solution, attributes)
    
    # TODO modify to .path & test
    xml = SolutionSpecification.make_xml(solution)
    config = solution.get_config_filepath()
    with open(config, "w") as xmlConfigFile:
        xmlConfigFile.write(xml.toprettyxml())
    
    return HttpResponseRedirect(reverse(index))

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
                config = solution.get_config_filepath()
                agents = json.loads(params[u'json'])
                # TODO Y U forgot to handle this?
                Validator.validateAgentMapping(agents)
                # TODO server-side validation goes here
                # also check IDs!!
                tree = SolutionSpecification.add_agents_to_xml(config, agents)
                tree.write(config)
                response = 'ok'
            except ValueError, e:
                response = str(e)

    return HttpResponse(response, mimetype="text/plain")

def get_solution_or_404(user, **kwargs):
    if not user.is_superuser:
        kwargs['envUser__user'] = user
    return get_object_or_404(Solution, **kwargs)
