from django.db import models
from envuser.models import EnvUser
from subenvironment.models import SubEnvironment

class TimePool(models.Model):
    envUser = models.ForeignKey(EnvUser)
    remaining = models.PositiveIntegerField(default=272)

    def __unicode__(self):
        uniArgs = (unicode(self.envUser), unicode(self.remaining))
        return '%s, %s left in pool' % uniArgs

class AbstractProcess(models.Model):
    envUser = models.ForeignKey(EnvUser)
    subEnvironment = models.ForeignKey(SubEnvironment)
    created = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        uniArgs = (self.envUser, self.subEnvironment, self.created)
        return 'process: user(%s) subenv(%s) created(%s)' % uniArgs
