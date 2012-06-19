from django.db import models
from envuser.models import EnvUser
from subenvironment.models import SubEnvironment
import os

def solution_upload_to(instance, filename):
    pathArgs = (instance.envUser.id, instance.subEnvironment.id, filename)
    return 'users/%s/solutions/%s/%s' % pathArgs

class SolutionManager(models.Manager):
    def get_sent_by_user_for_env(self, user, subEnvironment):
        queryset = self.get_query_set()
        qfilter = { 'subEnvironment': subEnvironment }
        if not user.is_superuser:
            qfilter['envUser__user'] = user
        return queryset.filter(**qfilter)

class Solution(models.Model):
    envUser = models.ForeignKey(EnvUser)
    subEnvironment = models.ForeignKey(SubEnvironment)
    file = models.FileField(upload_to=solution_upload_to)

    objects = SolutionManager()

    def __unicode__(self):
        filename = os.path.basename(self.file.name)
        uniArgs = (unicode(self.envUser), unicode(self.subEnvironment), filename)
        return 'Author(%s), Env(%s), File(%s)' % uniArgs
