from django.core.management.base import BaseCommand
from home.builder import build_side_projects

class Command(BaseCommand):
    def handle(self, *args, **kwargs):
        build_side_projects('side-projects')
