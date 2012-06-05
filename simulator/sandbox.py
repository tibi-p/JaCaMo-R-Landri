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

def absposixpath(file):
    file = os.path.abspath(file)
    if os.altsep != os.sep:
        file = file.replace(os.sep, os.altsep)
    return file

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

def extractZipFile(files, dir):
    for filename in files:
        with ZipFile(filename) as f:
            names = [ name for name in f.namelist() if
                not os.path.isabs(name) and os.pardir not in name ]
            f.extractall(dir, names)

class JaCaMoSandbox(object):
    def __init__(self, root):
        self.root = root

    def populate(self, agentFiles, files):
        agentDir = os.path.join(self.root, 'src', 'agents')
        silent_md(os.makedirs, agentDir)
        for source in agentFiles:
            dest = os.path.join(agentDir, os.path.basename(source))
            silent_md(os.makedirs, os.path.dirname(dest))
            shutil.copyfile(source, os.path.join(dest))

        for dirname, files in files.iteritems():
            dir = os.path.join(self.root, 'src', dirname)
            silent_md(os.makedirs, dir)
            extractZipFile(files, dir)

    def writeMAS(self, name, infra, env, agents):
        # create agents string
        ags = [ ]
        for agName, agent in agents.iteritems():
            fmtArgs = (agName, agent['arch'], agent['no'])
            ags.append('\t\t%s agentArchClass %s #%s;' % fmtArgs)
        ags = '\n'.join(ags)

        # create classpath string
        libs = filterListDir(os.path.join('lib', 'jacamo'), 'jar')
        path = '\n'.join('\t\t"%s";' % (absposixpath(lib),) for lib in libs)

        #template filling
        with open("templates/template.mas2j") as f:
            template = Template(f.read())
            contents = template.substitute(name=name,infra=infra,env=env,ags=ags,path=path)

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

    def ant(self):
        libraries = filterListDir(os.path.join('lib', 'ant'), 'jar')
        subprocess.call([
            "java",
            "-classpath",
            os.pathsep.join(libraries),
            "org.apache.tools.ant.launch.Launcher",
            "-f",
            os.path.join(self.root, 'bin', 'build.xml'),
        ])

    def clean(self):
        shutil.rmtree(os.path.join(self.root, 'src'), True)
        shutil.rmtree(os.path.join(self.root, 'bin'), True)
        for file in os.listdir(self.root):
            relfile = os.path.join(self.root, file)
            if checkExtension(relfile, 'mas2j'):
                os.remove(relfile)
