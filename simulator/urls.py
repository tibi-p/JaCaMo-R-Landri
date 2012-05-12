from django.conf.urls import patterns, url

urlpatterns = patterns('simulator.views',
    #url(r'^$', 'index'),
    url(r'^run/$', 'run'),
    url(r'^simulate/$', 'simulate'),
    url(r'^getSolutions/(?P<subEnvId>\d*)/$', 'getsolutions'),
)
