from django.conf.urls import patterns, url

urlpatterns = patterns('solution.views',
    url(r'^index/$', 'index'),
    url(r'^index/(?P<subEnvId>\d+)/$', 'index_post'),
    #url(r'^(?P<solutionId>\d+)/$', 'detail'),
)
