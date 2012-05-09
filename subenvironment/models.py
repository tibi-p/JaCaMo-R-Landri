from django.db import models
from city.models import Ring
import functools

class SubEnvironmentManager(models.Manager):
    def get_solved_by_user(self, user):
        queryset = self.get_query_set()
        filter = { 'solution__isnull': False }
        if not user.is_superuser:
            filter['solution__envUser__user'] = user
        return queryset.filter(**filter).distinct()

class SubEnvironment(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()
    ring = models.ForeignKey(Ring)

    objects = SubEnvironmentManager()

    def __unicode__(self):
        return self.name

def dir_upload_to(dir, instance, filename):
    pathArgs = (instance.subenvironment.id, dir, filename)
    return 'subenvironments/%s/%s/%s' % pathArgs

class BaseComponent(models.Model):
    subenvironment = models.ForeignKey(SubEnvironment)
    name = models.CharField(max_length=200)

    def __init__(self, *args, **kwargs):
        super(BaseComponent, self).__init__(*args, **kwargs)
        self.original_file = self.file

    def __unicode__(self):
        return self.name

    class Meta:
        abstract = True

class EnvAgent(BaseComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'agents'))

class Artifact(BaseComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'artifacts'))

class Organization(BaseComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'organizations'))

def file_post_save(sender, instance, created, **kwargs):
    if not created:
        instance.original_file.delete(save=False)
    instance.original_file = instance.file

def file_post_delete(sender, instance, **kwargs):
    instance.file.delete(save=False)

for sender in BaseComponent.__subclasses__():
    models.signals.post_save.connect(file_post_save, sender=sender)
    models.signals.post_delete.connect(file_post_delete, sender=sender)
