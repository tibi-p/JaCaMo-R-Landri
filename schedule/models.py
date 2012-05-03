from django.db import models
from solution.models import Solution

class Schedule(models.Model):
    solution = models.ForeignKey(Solution)

    def __unicode__(self):
        uniArgs = (unicode(self.solution),)
        return 'Schedule(%s)' % uniArgs

class FakeSchedule(models.Model):
    solution = models.ForeignKey(Solution)

    def __unicode__(self):
        uniArgs = (unicode(self.solution),)
        return 'FakeSchedule(%s)' % uniArgs
