from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvUser
from solution.models import Solution
from subenvironment.models import SubEnvironment
import django_tables2 as tables
from django_tables2.utils import A

class EnvUserTable(tables.Table):
    user = tables.LinkColumn('envuser.views.detail', args=[ A('pk') ],
        order_by=('user.username', '-rank'))

    class Meta:
        model = EnvUser
        fields = ('user', 'rank', 'economy')
        sequence = ('user', 'rank', 'economy')
        attrs = { 'style': 'text-align: center;' }
        order_by = ('-rank', 'user')

@login_required
def index(request):
    envUserTable = EnvUserTable(EnvUser.objects.all())
    tables.RequestConfig(request, paginate={
        'per_page': 25
    }).configure(envUserTable)
    return render_to_response('envuser/index.html',
        { 'envUserTable': envUserTable },
        context_instance = RequestContext(request))

@login_required
def detail(request, envUserId):
    envUser = get_object_or_404(EnvUser, pk=envUserId)
    user = envUser.user
    subEnvironments = SubEnvironment.objects.get_solved_by_user(user)
    solutions = Solution.objects
    allSolutions = [ ]
    for subEnvironment in subEnvironments:
        allSolutions.append({
            'subEnvironment': subEnvironment,
            'someSolutions': solutions.get_sent_by_user_for_env(user, subEnvironment),
        })
    return render_to_response('envuser/detail.html',
        {
            'envUser': envUser,
            'allSolutions': allSolutions,
        },
        context_instance = RequestContext(request))

@login_required
def profile(request):
    user = request.user
    if user.is_superuser:
        return index(request)
    else:
        envUser = get_object_or_404(EnvUser, user=user)
        return detail(request, envUser.pk)
