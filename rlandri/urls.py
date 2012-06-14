from django.conf import settings
from django.conf.urls import patterns, include, url
from django.contrib.staticfiles.urls import staticfiles_urlpatterns

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'rlandri.views.home', name='home'),
    # url(r'^rlandri/', include('rlandri.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),

    url(r'', include('django_socketio.urls')),

    url(r'^$', 'home.views.index', name='home'),

    url(r'^accounts/login/$', 'django.contrib.auth.views.login'),
    url(r'^accounts/logout/$', 'django.contrib.auth.views.logout'),

    url(r'^city/', include('city.urls')),

    url(r'^subenvironment/', include('subenvironment.urls')),

    url(r'^envuser/', include('envuser.urls')),

    url(r'^solution/', include('solution.urls')),

    url(r'^schedule/', include('schedule.urls')),

    url(r'^simulator/', include('simulator.urls')),
)

if settings.DEBUG:
    urlpatterns += patterns('',
        (r'^media/(?P<path>.*)$', 'django.views.static.serve',
            { 'document_root': settings.MEDIA_ROOT }),
    )
    urlpatterns += staticfiles_urlpatterns()
