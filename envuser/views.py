from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from envuser.models import EnvUser

@login_required
def index(request):
    envUserList = EnvUser.objects.all()
    return render_to_response('envuser/index.html',
        { 'envUserList': envUserList },
        context_instance = RequestContext(request))

@login_required
def detail(request, envUserId):
    envUser = get_object_or_404(EnvUser, pk = envUserId)
    return render_to_response('envuser/detail.html',
        { 'envUser': envUser },
        context_instance = RequestContext(request))
