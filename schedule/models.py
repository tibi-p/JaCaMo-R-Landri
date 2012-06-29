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

class Schedule(models.Model):
    solution = models.OneToOneField(Solution)
    turn = models.PositiveIntegerField()
    step = models.PositiveIntegerField()
    lastModified = models.DateTimeField(auto_now=True)

    objects = ScheduleManager()

    def __unicode__(self):
        uniArgs = (unicode(self.solution), self.turn, self.step)
        return '%s, #%s, @%s' % uniArgs
