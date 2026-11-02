from os.path import abspath, dirname, basename, join

DEBUG = True
TEMPLATE_DEBUG = DEBUG
THUMBNAIL_DEBUG = DEBUG 

ROOT_PATH = abspath(dirname(__file__))
PROJECT_NAME = basename(ROOT_PATH)

ADMINS = (
    # ('Your Name', 'your_email@domain.com'),
)
MANAGERS = ADMINS

DATABASES = {
    'default': {
	'ENGINE': 'django.db.backends.sqlite3',
        'NAME': join(ROOT_PATH, 'database/riskmaps.db'),
	#'ENGINE': 'django.db.backends.mysql',
        #'NAME': 'riskmaps',
        #'USER': 'a_user',
        #'PASSWORD': 'a_password',
        #'HOST': '',
        #'PORT': '',
    }
}

TIME_ZONE = 'America/Chicago'
LANGUAGE_CODE = 'en-us'
SITE_ID = 1

USE_I18N = True
# with USE_L10N every number in a template goes through a slow locale lookup (1000s per map list),
# the output is the same without it as we only use 'en-us'
USE_L10N = False

MEDIA_ROOT = join(ROOT_PATH, 'storage')
MEDIA_URL = '/storage/'
STATIC_DOC_ROOT = join(ROOT_PATH, 'static')
ADMIN_MEDIA_PREFIX = '/static/admin/'

SECRET_KEY = 't2eo^kd%k+-##ml3@_x__$j0(ps4p0q6eg*c4ttp9d2n(t!iol'

TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.Loader',
    'django.template.loaders.app_directories.Loader',
)

MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    #'debug_toolbar.middleware.DebugToolbarMiddleware',
)

ROOT_URLCONF = 'urls'

TEMPLATE_DIRS = (
    join(ROOT_PATH, 'templates')
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.admin',
    'social_auth',
    'app',
    'djangoratings',
    'riskmaps',
    #'sorl.thumbnail',
    'easy_thumbnails',
    #'debug_toolbar',
)

AUTHENTICATION_BACKENDS = (
    'social_auth.backends.twitter.TwitterBackend',
    'social_auth.backends.facebook.FacebookBackend',
    'social_auth.backends.google.GoogleOAuthBackend',
    'social_auth.backends.google.GoogleBackend',
    'social_auth.backends.yahoo.YahooBackend',
    'social_auth.backends.contrib.linkedin.LinkedinBackend',
    'social_auth.backends.OpenIDBackend',
    'social_auth.backends.contrib.livejournal.LiveJournalBackend',
    'django.contrib.auth.backends.ModelBackend',
)

LOGIN_URL = "/account"
LOGIN_REDIRECT_URL = "/"

#INTERNAL_IPS = ('127.0.0.1','217.138.10.27')


try:
    from local_settings import *
except:
    pass


