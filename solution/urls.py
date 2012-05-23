from django.conf.urls import patterns, url

urlpatterns = patterns('solution.views',
    url(r'^index/$', 'index'),
    #url(r'^(?P<solutionId>\d+)/$', 'detail'),
)
