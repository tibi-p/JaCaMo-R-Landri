from django.conf.urls import patterns, url

urlpatterns = patterns('city.views',
    url(r'^$', 'index'),
    #url(r'^(?P<subEnvironmentId>\d+)/$', 'detail'),
)
