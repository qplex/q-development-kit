import setuptools
import setuptools.command.build_ext as build_ext
import os
import subprocess
import sys
import argparse
import glob
import tempfile

"""
This module builds a Python extension module from Q source. 
It requires write access to the current working directory, where temporary files and extension modules are saved.

1. Example usage as a script:

        python3 qcompile.py module_name SourceFilename.q AnotherSourceFilename.q

    To enable logging, add the "-log" option:

        python3 qcompile.py -log module_name SourceFilename.q AnotherSourceFilename.q

    The resulting extension module can be imported from Python with the statement "import module_name".

2. Example usage from Python:

        from qcompile import QModule
        q_source = '''
        public Pmf truncationMove() {
            x ~ binomial(2, 0.2);
            int y = min(x, 1);
            return y;
        }
        '''
        q_module = QModule('module_name')
        q_module.add_q_class('ClassName', q_source)
        q_module.add_q_class('AnotherClassName', 'public int i;')
        q_module.build()

    To enable logging, replace the assignment that defines q_module by:

        q_module = QModule('module_name', log=True)

    The resulting extension module can be imported immediately thereafter with "import module_name".
"""


def _is_valid_module_name(name):
    if not isinstance(name, str) or not name or name[0].isdigit():
        return False
    return all(c.islower() or c == '_' or c.isdigit() for c in name)


def _is_valid_q_class_name(name):
    if not isinstance(name, str) or not name or not name[0].isupper():
        return False
    return name[1:].isalnum()


class QModule:
    def __init__(self, module_name, q_filenames=None, log=False):
        """
        Constructor for the Q module.

        Args:
            module_name (str): Name of the module. Can only contain lower-case letters, digits and underscores, and must start with a letter or an underscore.
            q_filenames (list): Files in current working directory that contain Q source and are to be built into the module.
                                Each file must have the extension ".q". It can only contain letters and digits, and must start with an upper-case letter.
            log (bool): Whether the output of the Q module will be logged.
        """
        if q_filenames is None:
            q_filenames = []
        if not _is_valid_module_name(module_name):
            raise ValueError('Invalid module name: ' + module_name)
        for q_filename in q_filenames:
            stem, extension = os.path.splitext(q_filename)
            if not extension == '.q':
                raise ValueError('Invalid extension: ' + q_filename)
            if not _is_valid_q_class_name(stem):
                raise ValueError('Invalid Q source name:' + q_filename)

        self.module_name = module_name
        self.q_filenames = q_filenames
        self.log = log

    def add_q_class(self, class_name, q_code):
        """
        Stages a class for inclusion into the extension module. Does not validate Q source.

        Args:
            class_name (str): Name of the class. Can only contain letters and digits, and must start with an upper-case letter.
            q_code (str): Q source code for the class.
        """
        if not _is_valid_q_class_name(class_name):
            raise ValueError('Invalid Q class name')
        
        with open(class_name + '.q', 'w') as f:
            f.write(q_code)
        self.q_filenames.append(class_name + '.q')

    def _compile_q(self):
        if self.log:
            java_command = ['java', '-jar', 'QCompiler.jar'] + ['-log'] + [self.module_name] + self.q_filenames
        else:
            java_command = ['java', '-jar', 'QCompiler.jar'] + [self.module_name] + self.q_filenames

        try:
            return subprocess.run(java_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        except:
            raise RuntimeError('Unable to run Java')

    def _compile_cpp(self):
        ext_modules = [setuptools.Extension(self.module_name, [self.module_name + '.cpp'])]
        for extension in ext_modules:
            # compilation fails if this is not set
            extension._needs_stub = False
        dist = setuptools.Distribution()
        build_extension = build_ext.build_ext(dist)
        build_extension.finalize_options()
        build_extension.extensions = ext_modules
        build_extension.build_lib = os.getcwd()

        with tempfile.NamedTemporaryFile(mode='w+') as temp_stderr:        
            try:
                saved_fd = os.dup(2)
                os.dup2(temp_stderr.fileno(), 2)
                
                build_extension.run()

                os.dup2(saved_fd, 2)
                os.close(saved_fd)
            except Exception: 
                temp_stderr.seek(0)
                print(temp_stderr.read())
                raise RuntimeError('An error has occurred. Please file a bug report on https://github.com/qplex/q-development-kit with your Q source and the above messages.')

    def build(self):
        """ 
        Creates C++ source code using the Q compiler and compiles this C++ code into an extension module.
        """
        if len(self.q_filenames) == 0:
            raise RuntimeError('Q source not available')

        java_result = self._compile_q()

        if java_result.returncode != 0:
            stderr = java_result.stderr.decode()
            stderr = stderr.replace('java -jar QCompiler.jar', 'python3 qcompile.py')
            print(stderr, end='')
            raise RuntimeError()
        else: 
            stdout = java_result.stdout.decode()
            print(stdout.replace('File', 'C++ source'), end='')

        self._compile_cpp()

        print('Extension module', self.module_name, 'with class(es)', 
              ', '.join([os.path.splitext(q_filename)[0] for q_filename in self.q_filenames]),
              'successfully compiled for Python', 
              str(sys.version_info.major) + '.' + str(sys.version_info.minor) + '.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Compile Q source files into an extension module.')
    parser.add_argument('-log', action='store_true', help='enable logging')
    parser.add_argument('module_name', help='name of the module')
    parser.add_argument('q_filename', nargs='+', help='Q source filename')
    args = parser.parse_args()

    # Expand wildcards (if present) regardless shell and operating system 
    q_filenames = []
    for pattern in args.q_filename:
        expanded = glob.glob(pattern)
        if expanded:
            q_filenames.extend(expanded)
        else:
            q_filenames.append(pattern)

    try:
        q_module = QModule(args.module_name, q_filenames, log=args.log)
    except ValueError as e:
        print(e)
        sys.exit(1)

    try:
        q_module.build()
    except RuntimeError as e:
        print(e)
        sys.exit(1)
