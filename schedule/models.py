from django.db import models
from solution.models import Solution

class ScheduleQuerySet(models.query.QuerySet):
    def get_solutions(self):
        solutions = set(entry.solution for entry in self)
        return list(solutions)

    def get_envusers(self):
        envusers = set(entry.solution.envUser for entry in self)
        return list(envusers)

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
        return '%s, #%s' % uniArgs

class Schedule(AbstractSchedule):
    step = models.PositiveIntegerField()
    lastModified = models.DateTimeField(auto_now=True)

    def __unicode__(self):
        uniArgs = (unicode(self.solution), self.numAgents, self.step)
        return '%s, #%s, @%s' % uniArgs
