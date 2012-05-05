from city.models import City, Ring
from django.contrib import admin

class RingInline(admin.TabularInline):
    model = Ring
    extra = 3

class CityAdmin(admin.ModelAdmin):
    inlines = [ RingInline ]

admin.site.register(City, CityAdmin)
