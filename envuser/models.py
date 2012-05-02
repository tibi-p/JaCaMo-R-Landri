from django.contrib.auth.models import User
from django.db import models
from django.db.models.signals import post_save

class EnvUser(models.Model):
    rank = models.BigIntegerField(default=0)
    economy = models.BigIntegerField(default=0)
    user = models.ForeignKey(User, unique=True)

    def __unicode__(self):
        return self.user.username

def create_user_profile(sender, instance, created, **kwargs):
    if created:
        EnvUser.objects.create(rank=0, economy=0, user=instance)

post_save.connect(create_user_profile, sender=User)
