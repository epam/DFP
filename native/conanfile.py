from conan import ConanFile
from conan.tools.files import copy
from conan.tools.cmake import CMakeToolchain, CMake, cmake_layout, CMakeDeps
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

    # Optional metadata
    description = "Decimal Floating Point Arithmetic Library"
    homepage = "https://github.com/epam/DFP/"
    url = "https://github.com/epam/DFP.git"
    license = ("Apache-2.0", "Intel")
    topics = ("decimal", "dfp", "ieee-754", "deltix")
    author = "Andrei Davydov (agdavydov81@gmail.com)"

    # Binary configuration
    settings = "os", "compiler", "build_type", "arch"
    options = {"shared": [True, False]}
    default_options = {"shared": False}

    # Sources are located in the same place as this recipe, copy them to the recipe
    exports_sources = "CMakeLists.txt", "src/*", "include/*"

    def set_version(self):
        # Command line ``--version=xxxx`` will be assigned first to self.version and have priority
        self.version = self.version or load_version(os.path.join(self.recipe_folder, "..", "gradle.properties"))

    def layout(self):
        cmake_layout(self)

    def generate(self):
        deps = CMakeDeps(self)
        deps.generate()
        tc = CMakeToolchain(self)
        tc.generate()

    def build(self):
        cmake = CMake(self)
        cmake.configure()
        cmake.build()

    def package(self):
        cmake = CMake(self)
        cmake.install()

    def package_info(self):
        self.cpp_info.libs = ["ddfp"]
