from django.db import models
from solution.models import Solution

class AbstractProcess(models.Model):
    solution = models.ForeignKey(Solution)
    created = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        uniArgs = (self.envUser, self.solution)
        return 'process: solution(%s)' % uniArgs
