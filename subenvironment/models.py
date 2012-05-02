from django.db import models

def artifact_upload_to(instance, filename):
    return 'subenvironments/artifacts/' + filename

def delete_artifact(instance, **kwargs):
    print 'Hw', instance
    pass

class Artifact(models.Model):
    name = models.CharField(max_length=200)
    file = models.FileField(upload_to=artifact_upload_to)

    def __unicode__(self):
        return self.name

models.signals.pre_delete.connect(delete_artifact, sender=Artifact)
