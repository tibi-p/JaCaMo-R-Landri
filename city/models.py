from django.db import models

class City(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()

    def __unicode__(self):
        return self.name

class Ring(models.Model):
    city = models.ForeignKey(City)
    index = models.PositiveIntegerField(blank=True)
    size = models.PositiveIntegerField()

    def __unicode__(self):
        uniArgs = (unicode(self.city), self.index, self.size)
        return 'City(%s) Ring(%s) Size(%s)' % uniArgs
