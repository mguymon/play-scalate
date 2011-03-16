# Scala
import sys,os,inspect,subprocess

from play.utils import *

MODULE = 'scalate'

COMMANDS = []

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
	
# ~~~~~~~~~~~~~~~~~~~~~~ New

def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    if command == 'new':
        module_dir = inspect.getfile(inspect.currentframe()).replace("commands.py","")
        shutil.copyfile(os.path.join(module_dir,'resources/Application.scala'), os.path.join(app.path, 'app/controllers/Application.scala'))
        shutil.copyfile(os.path.join(module_dir,'resources/Model.scala'), os.path.join(app.path, 'app/models/Model.scala'))
        shutil.copyfile(os.path.join(module_dir,'resources/index.ssp'), os.path.join(app.path, 'app/views/Application/index.ssp'))
        shutil.copyfile(os.path.join(module_dir,'resources/default.ssp'), os.path.join(app.path, 'app/views/default.ssp'))
        f = open(os.path.join(app.path, 'conf/application.conf'),'a')
        f.write('\n\n#scalate config\nscalate=ssp\njvm.memory=-Xmx256M -Xms32M')
        f.close()
        os.remove(os.path.join(app.path, 'app/views/Application/index.html'))
        os.remove(os.path.join(app.path, 'app/views/main.html'))


def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    if command == 'precompile':
        app.check()
        java_cmd = app.java_cmd(args)
        for arg in java_cmd:
            if(arg.find("-Xmx")==0):
                return
        java_cmd.insert(2, '-Xmx256M')

