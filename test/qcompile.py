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

output_a = java_result.stdout.decode()
output_b = java_result.stderr.decode()

if 'Usage' in output_a:
    output_a = output_a.replace('java -jar QCompiler.jar', 'python3 qcompile.py')

if 'Usage' in output_b:
    output_b = output_b.replace('java -jar QCompiler.jar', 'python3 qcompile.py')

print(output_a)
print(output_b)

if java_result.returncode != 0:
    sys.exit(java_result.returncode)

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
build_extension.run()

