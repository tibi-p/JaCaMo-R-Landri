from subenvironment.models import (OwnerRelationship, Agent, Artifact,
    Organization, SubEnvironment, DefaultExtra)
from django.contrib import admin

class OwnerRelationshipInline(admin.TabularInline):
    model = OwnerRelationship
    extra = 2

class AgentInline(admin.TabularInline):
    model = Agent
    extra = 3

class ArtifactInline(admin.TabularInline):
    model = Artifact
    extra = 3

class OrganizationInline(admin.TabularInline):
    model = Organization
    extra = 3

class SubEnvironmentAdmin(admin.ModelAdmin):
    inlines = [ OwnerRelationshipInline, AgentInline, ArtifactInline, OrganizationInline ]

admin.site.register(SubEnvironment, SubEnvironmentAdmin)
admin.site.register(DefaultExtra)
