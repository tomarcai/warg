from django.conf.urls.defaults import patterns, url, include
from django.contrib import admin

from app.views import home, done, logout, error
import settings

admin.autodiscover()

urlpatterns = patterns('',
    url(r'^account/$', home, name='home'),
    url(r'^done/$', done, name='done'), 
    url(r'^error/$', error, name='error'),
    url(r'^logout/$', logout, name='logout'), 
    url(r'^admin/', include(admin.site.urls)),
    url(r'', include('social_auth.urls')),
    
    (r'^upload$', 'riskmaps.views.upload_map'),
    (r'^upload-unauthorised$', 'riskmaps.views.upload_unauthorised_map'),
    (r'^count$', 'riskmaps.views.map_count'),


    (r'^maps/(?P<map_id>\d+)', 'riskmaps.views.view_map'),

    (r'^$', 'riskmaps.views.list_all_maps'),

    (r'^getMaps\.shtml$', 'riskmaps.views.list_all_maps'),

    (r'^maps$', 'riskmaps.views.list_all_maps'),
    (r'^categories', 'riskmaps.views.list_all_categories'),
)


if settings.DEBUG:
    urlpatterns += patterns('',
        (r'^static/(?P<path>.*)$', 'django.views.static.serve', {'document_root': settings.STATIC_DOC_ROOT}),
        (r'^storage/(?P<path>.*)$', 'django.views.static.serve', {'document_root': settings.MEDIA_ROOT}),
    )

