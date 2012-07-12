from django.db import models
from city.models import Ring
from envuser.models import EnvUser
from home.base import create_callback_post_delete, create_callback_post_save
import functools

class SubEnvironmentManager(models.Manager):
    def get_solved_by_user(self, user):
        queryset = self.get_query_set()
        qfilter = self.create_user_filter(user)
        return queryset.filter(**qfilter).distinct()

    def get_unsolved_by_user(self, user):
        queryset = self.get_query_set()
        qfilter = self.create_user_filter(user)
        return queryset.exclude(**qfilter).distinct()

    def create_user_filter(self, user):
        qfilter = { 'solution__isnull': False }
        if not user.is_superuser:
            qfilter['solution__envUser__user'] = user
        return qfilter

class SubEnvironment(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()
    ring = models.ForeignKey(Ring)
    index = models.PositiveIntegerField()
    owners = models.ManyToManyField(EnvUser, through='OwnerRelationship')
    envType = models.CharField(max_length=10, choices=[
        ('rtsp', 'Real-time Single-player'),
        ('rtmp', 'Real-time Multiplayer'),
        ('patb', 'Player-alternated Turn-based'),
        ('setb', 'Simultaneously-executed Turn-based'),
    ])
    coordinatorClass = models.CharField(max_length=200)

    objects = SubEnvironmentManager()

    def __unicode__(self):
        return self.name

class OwnerRelationship(models.Model):
    subEnvironment = models.ForeignKey(SubEnvironment)
    envUser = models.ForeignKey(EnvUser)
    shares = models.PositiveIntegerField()

def subenv_upload_to(args):
    return 'subenvironments/%s/%s/%s' % args

def dir_default_upload_to(directory, instance, filename):
    pathArgs = ('generic', directory, filename)
    return subenv_upload_to(pathArgs)

def dir_upload_to(directory, instance, filename):
    pathArgs = (instance.subenvironment.id, directory, filename)
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

class Agent(BaseFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'agents'))

class Artifact(BaseFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'artifacts'))

class Organization(BaseFileComponent):
    file = models.FileField(upload_to=functools.partial(dir_upload_to, 'organizations'))

file_post_save = create_callback_post_save('file')
file_post_delete = create_callback_post_delete('file')

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
