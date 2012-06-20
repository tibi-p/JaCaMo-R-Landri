from django.db import models
from envuser.models import EnvUser
from subenvironment.models import SubEnvironment

class AbstractProcess(models.Model):
    envUser = models.ForeignKey(EnvUser)
    subEnvironment = models.ForeignKey(SubEnvironment)
    created = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        uniArgs = (self.envUser, self.subEnvironment, self.created)
        return 'process: user(%s) subenv(%s) created(%s)' % uniArgs
