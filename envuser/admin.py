from envuser.models import EnvUser, EnvAgent
from django.contrib import admin

class EnvUserAdmin(admin.ModelAdmin):
    readonly_fields = [ 'user' ]

admin.site.register(EnvUser, EnvUserAdmin)
admin.site.register(EnvAgent)
