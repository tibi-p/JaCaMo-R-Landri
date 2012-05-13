from subenvironment.models import OwnerRelationship, EnvAgent, Artifact, Organization, SubEnvironment
from django.contrib import admin

class OwnerRelationshipInline(admin.TabularInline):
    model = OwnerRelationship
    extra = 2

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
    inlines = [ OwnerRelationshipInline, EnvAgentInline, ArtifactInline, OrganizationInline ]

admin.site.register(SubEnvironment, SubEnvironmentAdmin)
