from django import template
import os

register = template.Library()

@register.filter
def basename(file):
    return os.path.basename(file.name)

@register.filter
def filteruser(queryset, user):
    print queryset.filter(user=user)
    return queryset.filter(user=user)
