from django import template
import os

register = template.Library()

@register.filter
def basename(file):
    return os.path.basename(file.name)
