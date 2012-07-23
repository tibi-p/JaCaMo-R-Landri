'''
Build script for all side-projects.

Created on 3 Jul 2012

@author: Tiberiu Popa
'''

from simulator.sandbox import filter_listdir
import os
import subprocess

def build_sub_projects(root):
    "Build all projects from a directory"
    libraries = filter_listdir(os.path.join('lib', 'ant'), 'jar')
    build_xml = os.path.join(root, 'build.xml')
    if os.path.isfile(build_xml):
        print build_xml
        subprocess.call([
            "java",
            "-classpath",
            os.pathsep.join(libraries),
            "org.apache.tools.ant.launch.Launcher",
            "-f",
            build_xml,
        ])

def build_main(argv):
    build_sub_projects('side-projects')

if __name__ == '__main__':
    import sys
    build_main(sys.argv)
