from django.conf.urls import patterns, url

urlpatterns = patterns('simulator.views',
    #url(r'^$', 'index'),
    url(r'^run/$', 'run'),
    url(r'^schedule/$', 'schedule'),
    url(r'^runSimulation/(?P<section>\w+)/$', 'run_simulation'),
)
