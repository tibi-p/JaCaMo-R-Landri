import django_socketio.management.commands.runserver_socketio as runserver_socketio
from home.builder import build_side_projects

class Command(runserver_socketio.Command):
    def handle(self, addrport="", *args, **kwargs):
        build_side_projects('side-projects')
        super(Command, self).handle(addrport, args, kwargs)
