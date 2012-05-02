from envuser.models import EnvUser
from django.contrib import admin

class EnvUserAdmin(admin.ModelAdmin):
    readonly_fields = [ 'user' ]

admin.site.register(EnvUser, EnvUserAdmin)
