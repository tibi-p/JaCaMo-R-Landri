from django.conf.urls import patterns, url

urlpatterns = patterns('envuser.views',
    url(r'^$', 'index'),
    url(r'^(?P<envUserId>\d+)/$', 'detail'),
)
