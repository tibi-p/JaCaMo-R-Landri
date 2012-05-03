from django.db import models
from envuser.models import EnvUser
from subenvironment.models import SubEnvironment

def solution_upload_to(instance, filename):
    pathArgs = (instance.envUser.id, instance.subEnvironment.id, filename)
    return 'users/%s/solutions/%s/%s' % pathArgs

class Solution(models.Model):
    envUser = models.ForeignKey(EnvUser)
    subEnvironment = models.ForeignKey(SubEnvironment)
    file = models.FileField(upload_to=solution_upload_to)

    def __unicode__(self):
        uniArgs = (unicode(self.envUser), unicode(self.subEnvironment))
        return '%s @ %s' % uniArgs
