from django.db import models
from solution.models import Solution

class ScheduleQuerySet(models.query.QuerySet):
    def get_solutions(self):
        solutions = set(entry.solution for entry in self)
        return list(solutions)

class ScheduleManager(models.Manager):
    def get_query_set(self):
        return ScheduleQuerySet(self.model)

class AbstractSchedule(models.Model):
    solution = models.ForeignKey(Solution)
    numAgents = models.PositiveIntegerField()

    objects = ScheduleManager()

    class Meta:
        abstract = True

class OfflineTest(AbstractSchedule):
    def __unicode__(self):
        uniArgs = (unicode(self.solution), self.numAgents)
        return 'OfflineTest(%s, %s)' % uniArgs

class Schedule(AbstractSchedule):
    step = models.PositiveIntegerField()

    def __unicode__(self):
        uniArgs = (unicode(self.solution), self.numAgents)
        return 'Schedule(%s, %s)' % uniArgs
