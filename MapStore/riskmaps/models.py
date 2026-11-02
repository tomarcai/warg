from django.db import models, DatabaseError
from djangoratings.fields import RatingField
from django.contrib.auth.models import User
from datetime import datetime


def map_file_path(instance):
    if not instance.cuntDate:
        import datetime
        instance.cuntDate = datetime.datetime.now()

    return instance.cuntDate.strftime("%Y-%m-%d-%H-%M-%S")

def map_file_name(instance, filename):
    return map_file_path(instance) + "/" + filename

def preview_file_name(instance, filename):
    return map_file_path(instance) + "/preview/" + filename

class DailyStats(models.Model):
    date = models.DateField(blank=True, auto_now_add=True)
    numberOfDownloads = models.PositiveIntegerField(blank=True, default=0)

    class Meta:
        verbose_name = "Daily Stat"
        verbose_name_plural = "Daily Stats"

    @staticmethod
    def stat_for_today():
        stats = DailyStats.objects.filter(date=datetime.now())

        if len(stats) > 0:
            return stats[0]
        else:
            return DailyStats()

class GameCategory(models.Model):
    name = models.CharField(max_length=64,unique=True)
    icon = models.ImageField(upload_to='category-icons', blank=True)

    class Meta:
        verbose_name = "Category"
        verbose_name_plural = "Categories"

    def __unicode__(self):
        return self.name


class GameMap(models.Model):
    name = models.CharField(max_length=128)
    description = models.TextField()
    author = models.ForeignKey(User, blank=True)
    categories = models.ManyToManyField(GameCategory)

    version = models.PositiveIntegerField(blank=True, default=1)
    dateAdded = models.DateTimeField(blank=True, auto_now_add=True)

    cuntDate = None 

    # Popularity
    numberOfDownloads = models.PositiveIntegerField(blank=True, default=0)
    rating = RatingField(range=5, allow_anonymous=True, use_cookies=True)

    # Actual File Data
    outlinesFile = models.FileField(upload_to=map_file_name, blank=True)
    imageFile = models.ImageField(upload_to=map_file_name, blank=True)
    cardsFile = models.FileField(upload_to=map_file_name, blank=True)
    mapFile = models.FileField(upload_to=map_file_name, blank=True)
    prevFile = models.FileField(upload_to=preview_file_name, blank=True)

    #previewUrl = models.URLField()
    #mapUrl = models.URLField()
    # size of imageFile, read from the image the first time it is needed, clear these to read it again
    mapWidth = models.PositiveIntegerField(null=True, blank=True)
    mapHeight = models.PositiveIntegerField(null=True, blank=True)

    visible = models.BooleanField(default=False)

    class Meta:
        verbose_name = "Map"
        verbose_name_plural = "Maps"

#    def __init__(self):
#        import datetime
#        self.cuntDate = datetime.datetime.now()

    def __unicode__(self):
        if self.author:
            return self.name + " (By " + self.author.username + ")"
        else:
            return self.name

    def save(self, *args, **kwargs):
        super(GameMap, self).save(*args, **kwargs)

    def image_width(self):
        self.load_image_size()
        return self.mapWidth

    def image_height(self):
        self.load_image_size()
        return self.mapHeight

    def load_image_size(self):
        if self.mapWidth is None or self.mapHeight is None:
            # reading the size opens and parses the image file, so only do it once and store the result
            self.mapWidth, self.mapHeight = self.imageFile.width, self.imageFile.height
            try:
                GameMap.objects.filter(id=self.id).update(mapWidth=self.mapWidth, mapHeight=self.mapHeight)
            except DatabaseError:
                pass # e.g. database locked by another write, the size is still shown and stored next time


from django.db.backends.signals import connection_created

def add_missing_columns(sender, connection, **kwargs):
    # Django 1.2 has no migrations and syncdb only creates missing tables, so when a nullable field
    # is added to GameMap, add its column to an existing database the first time this process connects
    cursor = connection.cursor()
    table = GameMap._meta.db_table
    if table not in connection.introspection.get_table_list(cursor):
        return # syncdb has not created the table yet, and will create it with every column
    columns = [column[0] for column in connection.introspection.get_table_description(cursor, table)]
    quote = connection.ops.quote_name
    missing = [field for field in GameMap._meta.local_fields if field.null and field.column not in columns]
    for field in missing:
        try:
            cursor.execute('ALTER TABLE %s ADD COLUMN %s %s' % (quote(table), quote(field.column), field.db_type(connection=connection)))
        except DatabaseError:
            # another process may have added it at the same time, if not (e.g. the database was locked)
            # raise, so this request fails and the next connection tries again
            if field.column not in [column[0] for column in connection.introspection.get_table_description(cursor, table)]:
                raise
    connection_created.disconnect(add_missing_columns)

connection_created.connect(add_missing_columns)
