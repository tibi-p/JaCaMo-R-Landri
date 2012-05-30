from django.conf.urls import patterns, url

urlpatterns = patterns('simulator.views',
    #url(r'^$', 'index'),
    url(r'^run/$', 'run'),
    url(r'^simulate/$', 'simulate'),
    url(r'^simulate/(?P<subEnvId>\d*)/$', 'simulate_post'),
    url(r'^schedule/$', 'schedule'),
    url(r'^addNewTest/$', 'add_new_test'),
    url(r'^deleteTest/$', 'delete_test'),
    url(r'^getOtherSolutions/$', 'get_other_solutions'),
    url(r'^runSimulation/$', 'run_simulation'),
)
