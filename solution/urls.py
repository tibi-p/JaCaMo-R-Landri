from django.conf.urls import patterns, url

urlpatterns = patterns('solution.views',
    url(r'^index/$', 'index'),
    url(r'^index/change/(?P<solutionId>\d+)/$', 'index_change'),
    url(r'^index/add/(?P<subEnvId>\d+)/$', 'index_add'),
    url(r'^index/remove/(?P<solutionId>\d+)/$', 'index_remove'),
    #url(r'^(?P<solutionId>\d+)/$', 'detail'),
)
