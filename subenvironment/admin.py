from subenvironment.models import EnvAgent, Artifact, Organization, SubEnvironment
from django.contrib import admin

class EnvAgentInline(admin.TabularInline):
    model = EnvAgent
    extra = 3

class ArtifactInline(admin.TabularInline):
    model = Artifact
    extra = 3

class OrganizationInline(admin.TabularInline):
    model = Organization
    extra = 3

class SubEnvironmentAdmin(admin.ModelAdmin):
    inlines = [ EnvAgentInline, ArtifactInline, OrganizationInline ]

admin.site.register(SubEnvironment, SubEnvironmentAdmin)
