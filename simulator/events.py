from django.shortcuts import get_object_or_404
from django_socketio import events
from simulator.models import AbstractProcess
from simulator.turn import getSandboxProcess
from simulator.views import queryOfflineTests
import threading

class AbstractThread(threading.Thread):
    def __init__(self, user, socket, abstractProcess):
        super(AbstractThread, self).__init__()
        self.user = user
        self.socket = socket
        self.abstractProcess = abstractProcess

    def run(self):
        subEnvironment = self.abstractProcess.subEnvironment
        tests = queryOfflineTests(self.user, subEnvironment)
        process, pipes = getSandboxProcess(subEnvironment, tests, True)
        process.start()
        pipes[1].close()
        while True:
            try:
                msg = pipes[0].recv()
                print '(*-*): ' + msg
                self.socket.send(msg)
            except EOFError:
                break

@events.on_subscribe(channel="^abstract-process-")
def subscribe(request, socket, context, channel):
    user = request.user
    tokens = channel.split('-')

    if len(tokens) > 2:
        pid = tokens[2]
        abstractProcess = get_object_or_404(AbstractProcess, id=pid)
        thread = AbstractThread(user, socket, abstractProcess)
        thread.start()
