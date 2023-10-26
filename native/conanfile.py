from conan import ConanFile
from conan.tools.files import copy
#from conan.tools.cmake import CMakeToolchain, CMake, cmake_layout, CMakeDeps
import os
import re

from conan.tools.scm import git


# https://docs.conan.io/2/reference/conanfile/attributes.html


def load_version(properties_filename):
    with (open(properties_filename, 'r') as file):
        for line in file:
            if re.match("^\s*version\s*=", line):
                return line[line.index("=") + 1:].strip().removesuffix("-SNAPSHOT")

    raise Exception("Can't determine version")


class DfpConan(ConanFile):
    name = "dfp"
    package_type = "library"
    description = "Decimal Floating Point Arithmetic Library"
    homepage = "https://github.com/epam/DFP/"
    url = "https://github.com/epam/DFP.git"
    license = ("Apache-2.0", "Intel")
    topics = ("decimal", "dfp", "ieee-754", "deltix")
    author = "Andrei Davydov (agdavydov81@gmail.com)"
    settings = "os", "compiler", "build_type", "arch"

    options = {
        "shared": [True, False],
        "c_runtime": [None, "glibc", "musl", "libc"],
        "c_runtime_shared": [None, True, False],
        "libPath": [None, "ANY"],
        "libMask": [None, "ANY"]
    }
    default_options = {
        "shared": True,
        "c_runtime": None,
        "c_runtime_shared": None,
        "libMask": None
    }

    def set_version(self):
        # Command line ``--version=xxxx`` will be assigned first to self.version and have priority
        self.version = self.version or load_version(os.path.join(self.recipe_folder, "..", "gradle.properties"))

    def configure(self):
        # it's a C library
        self.settings.rm_safe("compiler.libcxx")
        self.settings.rm_safe("compiler.cppstd")

        self.libPath = f"{self.options.get_safe('libPath')}"
        self.libMask = f"{self.options.get_safe('libMask')}"
        self.options.rm_safe("libPath")
        self.options.rm_safe("libMask")

    def package(self):
        copy(self, pattern="DecimalNative.h*", src=self.recipe_folder, dst=os.path.join(self.package_folder, "include"))
        copy(self, pattern=self.libMask, src=self.libPath, dst=os.path.join(self.package_folder, "lib"))
