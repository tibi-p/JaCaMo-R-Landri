from subenvironment.models import Artifact, SubEnvironment
from django.contrib import admin

class ArtifactInline(admin.TabularInline):
    model = Artifact
    extra = 3

class SubEnvironmentAdmin(admin.ModelAdmin):
    inlines = [ ArtifactInline ]

admin.site.register(SubEnvironment, SubEnvironmentAdmin)
