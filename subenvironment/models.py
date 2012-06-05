from django.db import models
from city.models import Ring
from envuser.models import EnvUser
import functools

class SubEnvironmentManager(models.Manager):
    def get_solved_by_user(self, user):
        queryset = self.get_query_set()
        filter = self.create_user_filter(user)
        return queryset.filter(**filter).distinct()

    def get_unsolved_by_user(self, user):
        queryset = self.get_query_set()
        filter = self.create_user_filter(user)
        return queryset.exclude(**filter).distinct()

    def create_user_filter(self, user):
        filter = { 'solution__isnull': False }
        if not user.is_superuser:
            filter['solution__envUser__user'] = user
        return filter

class SubEnvironment(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()
    ring = models.ForeignKey(Ring)
    index = models.PositiveIntegerField()
    owners = models.ManyToManyField(EnvUser, through='OwnerRelationship')

    objects = SubEnvironmentManager()

    def __unicode__(self):
        return self.name

class OwnerRelationship(models.Model):
    subEnvironment = models.ForeignKey(SubEnvironment)
    envUser = models.ForeignKey(EnvUser)
    shares = models.PositiveIntegerField()

def subenv_upload_to(tuple):
    return 'subenvironments/%s/%s/%s' % tuple

def dir_default_upload_to(dir, instance, filename):
    pathArgs = ('generic', dir, filename)
    return subenv_upload_to(pathArgs)

def dir_upload_to(dir, instance, filename):
    pathArgs = (instance.subenvironment.id, dir, filename)
    return subenv_upload_to(pathArgs)

class BaseDefaultFileComponent(models.Model):
    name = models.CharField(max_length=200)

    def __init__(self, *args, **kwargs):
        super(BaseDefaultFileComponent, self).__init__(*args, **kwargs)
        self.original_file = self.file

    def __unicode__(self):
        return self.name

    class Meta:
        abstract = True

class DefaultExtra(BaseDefaultFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_default_upload_to, 'extra'))

class BaseFileComponent(BaseDefaultFileComponent):
    subenvironment = models.ForeignKey(SubEnvironment)

    class Meta:
        abstract = True

class EnvAgent(BaseFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'agents'))

class Artifact(BaseFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'artifacts'))

class Organization(BaseFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'organizations'))

def file_post_save(sender, instance, created, **kwargs):
    if not created:
        instance.original_file.delete(save=False)
    instance.original_file = instance.file

def file_post_delete(sender, instance, **kwargs):
    instance.file.delete(save=False)

def for_each_subclass(base, callback):
    if base._meta.abstract:
        children = base.__subclasses__()
        for child in children:
            for_each_subclass(child, callback)
    else:
        callback(base)

def set_file_signals(sender):
    models.signals.post_save.connect(file_post_save, sender=sender)
    models.signals.post_delete.connect(file_post_delete, sender=sender)

for_each_subclass(BaseDefaultFileComponent, set_file_signals)
