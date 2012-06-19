from django.contrib.auth.models import User
from django.db import models
from django.db.models.signals import post_save

class EnvUser(models.Model):
    rank = models.BigIntegerField(default=0)
    economy = models.BigIntegerField(default=0)
    user = models.ForeignKey(User, unique=True)

    def __unicode__(self):
        return self.user.username

class UserAgent(models.Model):
    name = models.CharField(max_length=200)
    envUser = models.ForeignKey(EnvUser)

    def __unicode__(self):
        uniArgs = (self.name, unicode(self.envUser))
        return '%s (%s)' % uniArgs

def create_user_profile(sender, instance, created, **kwargs):
    if created:
        EnvUser.objects.create(rank=0, economy=0, user=instance)

post_save.connect(create_user_profile, sender=User)
