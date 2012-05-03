from django.contrib import admin
from schedule.models import FakeSchedule, Schedule

admin.site.register(FakeSchedule)
admin.site.register(Schedule)
