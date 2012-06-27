from django.conf.urls import patterns, url

urlpatterns = patterns('solution.views',
    url(r'^index/$', 'index'),
    url(r'^index/change/(?P<solutionId>\d+)/$', 'index_change'),
    url(r'^index/add/(?P<subEnvId>\d+)/$', 'index_add'),
    url(r'^index/remove/(?P<solutionId>\d+)/$', 'index_remove'),
    url(r'^addAgent/$', 'add_agent'),
    url(r'^deleteAgent/$', 'delete_agent'),
    url(r'^changeAgentMapping/(?P<solutionId>\d+)/$', 'change_agent_mapping'),
    #url(r'^(?P<solutionId>\d+)/$', 'detail'),
)
