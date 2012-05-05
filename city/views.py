from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404, render_to_response
from django.template import RequestContext
from city.models import City

@login_required
def index(request):
    cityList = City.objects.all()
    return render_to_response('city/index.html',
        { 'cityList': cityList },
        context_instance = RequestContext(request))
