from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from subenvironment.models import SubEnvironment

@login_required
def index(request):
    subEnvironmentList = SubEnvironment.objects.all()
    return render_to_response('subenvironment/index.html',
        { 'subEnvironmentList': subEnvironmentList },
        context_instance = RequestContext(request))

@login_required
def detail(request, subEnvironmentId):
    subEnvironment = get_object_or_404(SubEnvironment, pk=subEnvironmentId)
    return render_to_response('subenvironment/detail.html',
        { 'subEnvironment': subEnvironment },
        context_instance = RequestContext(request))
