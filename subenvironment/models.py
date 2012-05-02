import logging
from django.db import models

logger = logging.getLogger(__name__)

class SubEnvironment(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()

    def __unicode__(self):
        return '%s (%s)' % (self.name, self.description)

def artifact_upload_to(instance, filename):
    pathArgs = (instance.subenvironment.id, filename)
    return 'subenvironments/%s/artifacts/%s' % pathArgs

class Artifact(models.Model):
    subenvironment = models.ForeignKey(SubEnvironment)
    name = models.CharField(max_length=200)
    file = models.FileField(upload_to=artifact_upload_to)

    def __init__(self, *args, **kwargs):
        super(Artifact, self).__init__(*args, **kwargs)
        self.original_file = self.file

    def __unicode__(self):
        return self.name

def artifact_post_save(sender, instance, created, **kwargs):
    if not created:
        instance.original_file.delete(save=False)
    instance.original_file = instance.file

def artifact_post_delete(sender, instance, **kwargs):
    instance.file.delete(save=False)

models.signals.post_save.connect(artifact_post_save, sender=Artifact)
models.signals.post_delete.connect(artifact_post_delete, sender=Artifact)
