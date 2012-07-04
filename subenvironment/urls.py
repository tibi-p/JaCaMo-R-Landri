from django.conf.urls import patterns, url

urlpatterns = patterns('subenvironment.views',
    url(r'^$', 'index'),
    url(r'^(?P<subEnvironmentId>\d+)/$', 'detail'),
    url(r'^process/(?P<processId>\d*)/$', 'simulate_process'),
)
