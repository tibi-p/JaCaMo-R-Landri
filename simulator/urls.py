from django.conf.urls import patterns, url

urlpatterns = patterns('simulator.views',
    #url(r'^$', 'index'),
    #url(r'^(?P<simulatorId>\d+)/$', 'run'),
    url(r'^simulate/$', 'simulate'),
)
