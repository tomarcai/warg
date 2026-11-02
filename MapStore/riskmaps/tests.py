from django.test import TestCase
from django.conf import settings
from django.db import connection
from django.contrib.auth.models import User
from riskmaps.models import GameMap


class MapListTest(TestCase):

    def setUp(self):
        self.author = User.objects.create(username='author')

    def add_map(self, file_name, visible=True):
        return GameMap.objects.create(
            name=file_name,
            description='a map',
            author=self.author,
            mapFile='2012-01-01-00-00-00/' + file_name,
            visible=visible)

    def get_map_urls(self, response):
        import re
        return re.findall(r'mapUrl="([^"]*)"', response.content)

    def test_search_by_mapfile(self):
        self.add_map('risk.map')
        self.add_map('Baldur\'s Gate.map')
        self.add_map('earth, sun, moon.map')
        self.add_map('Brazil_more(original)_bonuses.map')
        self.add_map('Gilbert@War.map')
        self.add_map('not requested.map')
        self.add_map('hidden.map', visible=False)

        response = self.client.post('/maps?format=xml&version=1.3.6beta1', {'mapfile': [
            'risk.map',
            'Baldur\'s Gate.map',
            'earth, sun, moon.map',
            'Brazil_more(original)_bonuses.map',
            'Gilbert@War.map',
            'hidden.map',
            'missing.map',
        ]})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(sorted(self.get_map_urls(response)), sorted([
            '/storage/2012-01-01-00-00-00/risk.map',
            '/storage/2012-01-01-00-00-00/Baldur&#39;s Gate.map',
            '/storage/2012-01-01-00-00-00/earth, sun, moon.map',
            '/storage/2012-01-01-00-00-00/Brazil_more(original)_bonuses.map',
            '/storage/2012-01-01-00-00-00/Gilbert@War.map',
        ]))

    def test_search_by_mapfile_does_not_treat_names_as_patterns(self):
        self.add_map('riskXmap.map')
        self.add_map('a_b.map')
        self.add_map('a%b.map')

        response = self.client.post('/maps?format=xml', {'mapfile': ['risk.map', 'a%b.map']})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(self.get_map_urls(response), ['/storage/2012-01-01-00-00-00/a%b.map'])

    def test_search_by_many_mapfiles(self):
        # the game client sends every map it has installed to check for updates,
        # this can be well over 1000 map files in a single request
        for i in range(0, 1500, 100):
            self.add_map('map %d.map' % i)

        map_files = ['map %d.map' % i for i in range(1500)]

        response = self.client.post('/maps?format=xml&version=1.3.6beta1', {'mapfile': map_files})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(self.get_map_urls(response)), 15)

    def count_queries(self, url):
        # connection.queries is only recorded when DEBUG is on, and the test runner turns it off
        old_debug = settings.DEBUG
        settings.DEBUG = True
        connection.queries = []
        try:
            response = self.client.get(url)
            self.assertEqual(response.status_code, 200)
            return len(connection.queries)
        finally:
            settings.DEBUG = old_debug

    def add_maps_with_new_authors(self, count):
        for i in range(count):
            self.author = User.objects.create(username='author %d' % User.objects.count())
            self.add_map('map %d.map' % GameMap.objects.count())

    def assert_query_count_does_not_grow(self, url):
        self.add_maps_with_new_authors(5)
        queries = self.count_queries(url)
        self.add_maps_with_new_authors(10)
        self.assertEqual(self.count_queries(url), queries)

    def test_list_xml_query_count_does_not_grow_with_maps(self):
        self.assert_query_count_does_not_grow('/?format=xml&sort=TOP_NEW')

    def test_list_html_query_count_does_not_grow_with_maps(self):
        self.assert_query_count_does_not_grow('/?sort=TOP_NEW')

    def test_list_xml_numbers(self):
        self.author = User.objects.create(username='someone', first_name='Some', last_name='One')
        game_map = self.add_map('risk.map')
        GameMap.objects.filter(id=game_map.id).update(numberOfDownloads=1234567, version=12)

        response = self.client.get('/?format=xml')

        self.assertEqual(response.status_code, 200)
        self.assertTrue('<Integer value="1"/>' in response.content)
        self.assertTrue('id="%d"' % game_map.id in response.content)
        self.assertTrue('authorId="%d"' % self.author.id in response.content)
        self.assertTrue('authorName="Some One"' in response.content)
        self.assertTrue('numberOfDownloads="1234567"' in response.content)
        self.assertTrue('version="12"' in response.content)


class MapImageTest(TestCase):

    def setUp(self):
        import StringIO, tempfile
        from PIL import Image
        from django.core.files.base import ContentFile
        from django.core.files.storage import default_storage
        from easy_thumbnails.files import DEFAULT_THUMBNAIL_STORAGE
        # keep the test images and their thumbnails out of the real storage folder
        self.storages = [default_storage, DEFAULT_THUMBNAIL_STORAGE]
        self.locations = [storage.location for storage in self.storages]
        self.dir = tempfile.mkdtemp()
        for storage in self.storages:
            storage.location = self.dir
        image = StringIO.StringIO()
        Image.new('RGB', (677, 425), (10, 120, 200)).save(image, 'JPEG')
        self.image_name = default_storage.save('2012-01-01-00-00-00/risk.jpg', ContentFile(image.getvalue()))
        self.image_path = default_storage.path(self.image_name)
        GameMap.objects.create(name='risk', description='a map', author=User.objects.create(username='author'),
            mapFile='2012-01-01-00-00-00/risk.map', imageFile=self.image_name, visible=True)

    def tearDown(self):
        import shutil
        for storage, location in zip(self.storages, self.locations):
            storage.location = location
        shutil.rmtree(self.dir)

    def get_size(self):
        import re
        response = self.client.get('/maps?format=xml&sort=TOP_NEW')
        self.assertEqual(response.status_code, 200)
        return re.findall(r'mapWidth="([^"]*)"\s+mapHeight="([^"]*)"', response.content)[0]

    def test_image_size_read_and_stored_when_missing(self):
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (None, None))
        self.assertEqual(self.get_size(), ('677', '425'))
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (677, 425))

    def test_stored_image_size_used_without_reading_image(self):
        import __builtin__
        self.get_size()  # the first listing generates the thumbnail, which needs to read the image
        GameMap.objects.update(mapWidth=1000, mapHeight=500)

        opened = []
        real_open = __builtin__.open
        def counting_open(path, *args, **kwargs):
            opened.append(path)
            return real_open(path, *args, **kwargs)
        __builtin__.open = counting_open
        try:
            self.assertEqual(self.get_size(), ('1000', '500'))
        finally:
            __builtin__.open = real_open
        self.assertEqual(opened.count(self.image_path), 0)

        # clearing the stored size makes it read from the image again
        GameMap.objects.update(mapWidth=None, mapHeight=None)
        self.assertEqual(self.get_size(), ('677', '425'))
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (677, 425))

    def test_image_size_shown_when_storing_it_fails(self):
        from django.db import DatabaseError
        from django.db.models.query import QuerySet
        def locked_update(self, **kwargs):
            raise DatabaseError('database is locked')
        real_update = QuerySet.update
        QuerySet.update = locked_update
        try:
            self.assertEqual(self.get_size(), ('677', '425'))
        finally:
            QuerySet.update = real_update
        # not stored, so the next listing tries again
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (None, None))
        self.assertEqual(self.get_size(), ('677', '425'))
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (677, 425))


class AddMissingColumnsTest(TestCase):

    def setUp(self):
        import os, tempfile
        from django.core.management.color import no_style
        from django.db.backends.signals import connection_created
        from django.db.backends.sqlite3.base import DatabaseWrapper
        from riskmaps.models import add_missing_columns

        handle, self.path = tempfile.mkstemp(suffix='.db')
        os.close(handle)
        # a short lock timeout, so a locked database fails fast instead of waiting 5 seconds
        self.settings_dict = dict(connection.settings_dict, NAME=self.path, OPTIONS={'timeout': 0.1})

        # a database made before mapWidth and mapHeight were added
        old_database = DatabaseWrapper(self.settings_dict)
        create_table = connection.creation.sql_create_model(GameMap, no_style(), set())[0][0]
        create_table = '\n'.join(line for line in create_table.split('\n') if '"mapWidth"' not in line and '"mapHeight"' not in line)
        old_database.cursor().execute(create_table)
        self.assertFalse('mapWidth' in self.columns(old_database))
        old_database.close()

        connection_created.connect(add_missing_columns)

    def tearDown(self):
        import os
        os.remove(self.path)

    def connect(self):
        from django.db.backends.sqlite3.base import DatabaseWrapper
        database = DatabaseWrapper(self.settings_dict)
        database.cursor()
        return database

    def columns(self, database):
        return [column[0] for column in database.introspection.get_table_description(database.cursor(), GameMap._meta.db_table)]

    def test_missing_columns_added_on_connect(self):
        database = self.connect()
        columns = self.columns(database)
        self.assertTrue('mapWidth' in columns)
        self.assertTrue('mapHeight' in columns)
        database.close()

    def test_columns_added_on_next_connect_if_database_was_locked(self):
        import sqlite3
        from django.db import DatabaseError
        # another process in the middle of a write, which still lets this process read
        writer = sqlite3.connect(self.path, isolation_level=None)
        writer.execute('BEGIN IMMEDIATE')
        try:
            self.assertRaises(DatabaseError, self.connect)
        finally:
            writer.execute('COMMIT')
            writer.close()

        database = self.connect()
        columns = self.columns(database)
        self.assertTrue('mapWidth' in columns)
        self.assertTrue('mapHeight' in columns)
        database.close()
