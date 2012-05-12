from django.db import models
from solution.models import Solution

class OfflineTest(models.Model):
    solution = models.ForeignKey(Solution)
    numAgents = models.PositiveIntegerField()

    def __unicode__(self):
        uniArgs = (unicode(self.solution), self.numAgents)
        return 'OfflineTest(%s, %s)' % uniArgs

class Schedule(OfflineTest):
    step = models.PositiveIntegerField()

    def __unicode__(self):
        uniArgs = (unicode(self.solution), self.numAgents)
        return 'Schedule(%s, %s)' % uniArgs
