from django.db import models
from envuser.models import EnvUser
from home.base import create_callback_post_delete, create_callback_post_save
from subenvironment.models import SubEnvironment
import os

def solution_upload_to(instance, filename):
    pathArgs = (instance.envUser.id, instance.subEnvironment.id, filename)
    return 'users/%s/solutions/%s/%s' % pathArgs

def get_config_filename(solution):
    basename = 'config_%s.xml' % (solution.id,)
    return solution_upload_to(solution, basename)

class SolutionManager(models.Manager):
    def get_sent_by_user_for_env(self, user, subEnvironment):
        queryset = self.get_query_set()
        qfilter = { 'subEnvironment': subEnvironment }
        if not user.is_superuser:
            qfilter['envUser__user'] = user
        return queryset.filter(**qfilter)

class Solution(models.Model):
    name = models.CharField(max_length=200)
    envUser = models.ForeignKey(EnvUser)
    subEnvironment = models.ForeignKey(SubEnvironment)
    description = models.TextField()
    isVisible = models.BooleanField()
    agents = models.FileField(upload_to=solution_upload_to)
    artifacts = models.FileField(upload_to=solution_upload_to)
    organizations = models.FileField(upload_to=solution_upload_to)
    lastModified = models.DateTimeField(auto_now=True)

    objects = SolutionManager()

    def __init__(self, *args, **kwargs):
        super(Solution, self).__init__(*args, **kwargs)
        self.original_agents = self.agents
        self.original_artifacts = self.artifacts
        self.original_organizations = self.organizations

    def __unicode__(self):
        agents = os.path.basename(self.agents.name)
        artifacts = os.path.basename(self.artifacts.name)
        organizations = os.path.basename(self.organizations.name)
        uniArgs = (self.name, agents, artifacts, organizations)
        return '%s (%s, %s, %s)' % uniArgs

    def get_config_filepath(self):
        from django.core.files.storage import default_storage
        filename = get_config_filename(self)
        return default_storage.path(filename)

file_post_saves = [ ]
file_post_deletes = [ ]

for field in [ 'agents', 'artifacts', 'organizations' ]:
    file_post_saves.append(create_callback_post_save(field))
    file_post_deletes.append(create_callback_post_delete(field))

# TODO implement me
def config_post_save(sender, instance, created, **kwargs):
    pass

def config_post_delete(sender, instance, **kwargs):
    try:
        os.remove(instance.get_config_filepath())
    except OSError, e:
        # TODO log me
        print e

for file_post_save in file_post_saves:
    models.signals.post_save.connect(file_post_save, sender=Solution)
models.signals.post_save.connect(config_post_save, sender=Solution)

for file_post_delete in file_post_deletes:
    models.signals.post_delete.connect(file_post_delete, sender=Solution)
models.signals.post_delete.connect(config_post_delete, sender=Solution)
