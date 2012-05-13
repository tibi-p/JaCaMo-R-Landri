from django.db import models
from envuser.models import EnvUser

class TimePool(models.Model):
    envUser = models.ForeignKey(EnvUser)
    remaining = models.PositiveIntegerField(default=272)

    def __unicode__(self):
        uniArgs = (unicode(self.envUser), unicode(self.remaining))
        return '%s, %s left in pool' % uniArgs
