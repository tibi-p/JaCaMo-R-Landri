import errno
import functools
import os
import shutil
import subprocess
from string import Template
from zipfile import ZipFile

def silent_md(f, path):
    try:
        return f(path)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

def absposixpath(filename):
    filename = os.path.abspath(filename)
    if os.altsep is not None and os.altsep != os.sep:
        filename = filename.replace(os.sep, os.altsep)
    return filename

def checkExtension(filename, ext):
    if os.path.isfile(filename):
        extPair = os.path.splitext(filename)
        return extPair[1] == ('.%s' % ext)
    else:
        return False

def filterListDir(directory, ext):
    files = os.listdir(directory)
    files = map(functools.partial(os.path.join, directory), files)
    return filter(functools.partial(checkExtension, ext=ext), files)

def transferFiles(files, directory):
    for source in files:
        if checkExtension(source, 'zip'):
            with ZipFile(source) as f:
                names = [ name for name in f.namelist() if
                    not os.path.isabs(name) and os.pardir not in name ]
                f.extractall(directory, names)
        else:
            dest = os.path.join(directory, os.path.basename(source))
            shutil.copyfile(source, os.path.join(dest))

class JaCaMoSandbox(object):
    def __init__(self, root):
        self.root = root

    def populate(self, solutionFiles, subenvFiles):
        for dirname, filelist in solutionFiles.iteritems():
            directory = os.path.join(self.root, 'src', dirname)
            silent_md(os.makedirs, directory)
            transferFiles(filelist, directory)

        for dirname, filelist in subenvFiles.iteritems():
            directory = os.path.join(self.root, 'src', dirname)
            silent_md(os.makedirs, directory)
            transferFiles(filelist, directory)

    def writeMAS(self, name, infra, env, agents):
        # create agents string
        ags = [ ]
        for agent in agents:
            if 'arch' in agent:
                agTpl = '\t\t%(name)s %(code)s agentArchClass %(arch)s #%(no)s;'
            else:
                agTpl = '\t\t%(name)s %(code)s #%(no)s;'
            ags.append(agTpl % agent)
        ags = '\n'.join(ags)

        # create classpath string
        libs = filterListDir(os.path.join('lib', 'jacamo'), 'jar')
        path = '\n'.join('\t\t"%s";' % (absposixpath(lib),) for lib in libs)

        #template filling
        with open("templates/template.mas2j") as f:
            template = Template(f.read())
            contents = template.substitute(name=name, infra=infra, env=env, ags=ags, path=path)

        filename = '%s.mas2j' % (name,)
        with open(os.path.join(self.root, filename), 'w') as g:
            g.write(contents)
        return filename

    def buildMAS(self, name):
        path = os.path.join(self.root, name)
        libraries = filterListDir(os.path.join('lib', 'jacamo'), 'jar')
        subprocess.call([
            "java",
            "-classpath",
            os.pathsep.join(libraries),
            "jason.mas2j.parser.mas2j",
            path,
        ])

    def ant(self, pipe):
        libraries = filterListDir(os.path.join('lib', 'ant'), 'jar')
        popenArgs = { }
        if pipe is not None:
            popenArgs['stdout'] = subprocess.PIPE
        masProcess = subprocess.Popen([
            "java",
            "-classpath",
            os.pathsep.join(libraries),
            "org.apache.tools.ant.launch.Launcher",
            "-f",
            os.path.join(self.root, 'bin', 'build.xml'),
        ], **popenArgs)
        if pipe is not None:
            while True:
                line = masProcess.stdout.readline()
                if line:
                    line = line.rstrip()
                    pipe.send(line)
                else:
                    break
            pipe.close()
        return masProcess.wait()

    def clean(self):
        shutil.rmtree(os.path.join(self.root, 'src'), True)
        shutil.rmtree(os.path.join(self.root, 'bin'), True)
        for filename in os.listdir(self.root):
            relfile = os.path.join(self.root, filename)
            if checkExtension(relfile, 'mas2j'):
                os.remove(relfile)
