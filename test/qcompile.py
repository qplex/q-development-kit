import setuptools
import setuptools.command.build_ext as build_ext
import os
import subprocess
import sys

try:
    java_result = subprocess.run(
        ['java', '-jar', 'QCompiler.jar'] + sys.argv[1:], 
        stdout=subprocess.PIPE, 
        stderr=subprocess.PIPE
    )
except:
    print('Unable to run Java')
    sys.exit(1)

if java_result.returncode != 0:
    stderr = java_result.stderr.decode()
    stderr = stderr.replace('java -jar QCompiler.jar', 'python3 qcompile.py')
    print(stderr, end='')
    sys.exit(java_result.returncode)
else: 
    stdout = java_result.stdout.decode()
    print(stdout.replace('File', 'C++ source'), end='')

module_name = sys.argv[1]
if module_name == '-log':
    module_name = sys.argv[2]

ext_modules = [setuptools.Extension(module_name, [module_name + '.cpp'])]
for extension in ext_modules:
    # compilation fails if this is not set
    extension._needs_stub = False
dist = setuptools.Distribution()
build_extension = build_ext.build_ext(dist)
build_extension.finalize_options()
build_extension.extensions = ext_modules
build_extension.build_lib = os.getcwd()

# direct C++ compiler's stderr to null and run it
null_fd =  os.open(os.devnull, os.O_RDWR)
saved_fd = os.dup(2)
os.dup2(null_fd, 2)
build_extension.run() 
os.dup2(saved_fd, 2)
os.close(null_fd)
os.close(saved_fd)

print('Extension module', module_name, 'successfully compiled.')
