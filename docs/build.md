# How to Build

# Table of Contents

* [Sources clone](#SourcesClone)
* [Building from source](#BuildingFromSource)
* [Java-only DFP-only build and test](#JavaOnlyDfpOnlyBuild)
* [Complete build and test](#CompleteBuild)
    - [Native libraries compilation and source wrappers generation](#NativeLibrariesCompilationAndSourceWrappersGeneration)
        + [Build Linux native libraries](#BuildLinuxNativeLibraries)
        + [Build Windows native libraries](#BuildWindowsNativeLibraries)
        + [Build macOS native libraries](#BuildMacOSNativeLibraries)
        + [Generate source wrappers](#GenerateSourceWrappers)
    - [Native libraries compression](#NativeLibrariesCompression)
    - [Java compilation](#JavaCompilation)
    - [.NET compilation](#NetCompilation)

## Sources clone<a name="SourcesClone"></a>

The whole project includes some modules. So, you need to clone repository with modules for example with next command:

```git
git clone --recursive https://github.com/epam/DFP.git DFP
```

## Building from source<a name="BuildingFromSource"></a>

All build steps are require `Java8` or newer for execution.\
The `Ubuntu 20.04` and `Windows 10` are assumed as the build systems.\
The `amd64` is assumed as the build platform.

The whole DFP project consist of multiple targets:

* DFP for Java
* DFP-Math for Java
* DFP for .NET
* DFP-Math for .NET

All of these targets, except DFP for Java, are dependent on the native libraries. The native libraries for all the
supported platforms must be compiled and compressed before Java/.NET compilation.

The only exclusion is the DFP for Java. The whole code, required for DFP for Java was ported from C. So if you
wish only to build DFP for Java and run some units tests, you do not need any except this repository sources and JDK.

Let's deeply investigate these two build options:

* [Java-only DFP-only build](#JavaOnlyDfpOnlyBuild)
* [Complete build](#CompleteBuild)

## Java-only DFP-only build and test<a name="JavaOnlyDfpOnlyBuild"></a>

The `JDK8` or newer is required for this compilation step.

The Java-only DFP and unit tests are placed to the `:java:dfp` project.\
So, you can execute all the targets in this project. For example

```shell
./gradlew :java:dfp:build
```

All the dependencies will be compiled automatically, so you do not need a separate software except JDK.

Unfortunately, not all the DFP test are native-free. Some unit tests are require native libraries. The native-dependent
tests and JMH benchmarks are separated to the `:java:dfpNativeTests` project. So, if you plan to run all the DFP tests
or benchmarks, see the [Complete build](#CompleteBuild) section.

## Complete build and test<a name="CompleteBuild"></a>

The complete build includes next steps:

* [Native libraries compilation and source wrappers generation](#NativeLibrariesCompilationAndSourceWrappersGeneration)
    - [Build Linux native libraries](#BuildLinuxNativeLibraries)
    - [Build Windows native libraries](#BuildWindowsNativeLibraries)
    - [Build macOS native libraries](#BuildMacOSNativeLibraries)
    - [Generate source wrappers](#GenerateSourceWrappers)
* [Native libraries compression](#NativeLibrariesCompression)
* [Java compilation](#JavaCompilation)
* [.NET compilation](#NetCompilation)

### Native libraries compilation and source wrappers generation<a name="NativeLibrariesCompilationAndSourceWrappersGeneration"></a>

#### Build Linux native libraries<a name="BuildLinuxNativeLibraries"></a>

This step assumed is executed on `Ubuntu 20.04` or newer on the `amd64` platform.

This steps includes compilation linux native libraries for the `armv7a`, `aarch64`, `i686`, `amd64` platforms,
so the cross-compilation is used.

Next packages are required for compilation

```shell
sudo apt install curl tar build-essential cmake g++-9-arm-linux-gnueabihf g++-9-aarch64-linux-gnu g++-9-i686-linux-gnu musl-tools
```

Also, the `clang` cross-compilers are required

```shell
mkdir ../llvm
cd ../llvm

curl -OL https://github.com/llvm/llvm-project/releases/download/llvmorg-12.0.0/clang+llvm-12.0.0-armv7a-linux-gnueabihf.tar.xz
curl -OL https://github.com/llvm/llvm-project/releases/download/llvmorg-12.0.0/clang+llvm-12.0.0-aarch64-linux-gnu.tar.xz
curl -OL https://github.com/llvm/llvm-project/releases/download/llvmorg-12.0.0/clang+llvm-12.0.0-i386-unknown-freebsd12.tar.xz

tar -xf clang+llvm-12.0.0-armv7a-linux-gnueabihf.tar.xz
tar -xf clang+llvm-12.0.0-aarch64-linux-gnu.tar.xz
tar -xf clang+llvm-12.0.0-i386-unknown-freebsd12.tar.xz

cd ../DFP/
```

Final step perform actual compilation

```shell
./gradlew makeNativeLinux
```

The compiled files will be places in the next folders:

* `native/bin/Release/linux` contain Linux native libraries for DFP
* `native/binmath/Release/linux` contain Linux native libraries for DFP-Math
* `native/binDemo/Release/linux` contain Linux executable console programs to test how the built programs work in
  different environments

#### Build Windows native libraries<a name="BuildWindowsNativeLibraries"></a>

This step assumed is executed on `Windows 10` or newer on the `amd64` platform.

This steps includes compilation windows native libraries for the `i686` and `amd64` platforms.

Next software are required for compilation

- Microsoft Visual C++ 2019 Build Tools (as a separate package or as a part of Visual Studio 2019) with `ClangCL`
  platform toolset
- CMake 3.20 or newer

So the all dependencies are meet, the compilation step (from the Developer Command Prompt) can be performed with the
command

```shell
gradlew makeNativeWindows
```

The compiled files will be places in the next folders:

* `native/bin/Release/windows` contain Windows native libraries for DFP
* `native/binmath/Release/windows` contain Windows native libraries for DFP-Math
* `native/binDemo/Release/windows` contain Windows executable console programs to test how the built programs work in
  different environments

#### Build macOS native libraries<a name="BuildMacOSNativeLibraries"></a>

For now the `macOS` native libraries build via cross-compilation on the `Ubuntu 20.04` or newer on the `amd64` platform.

Next packages are required for compilation

```shell
sudo apt install clang make libssl-dev liblzma-dev libz-dev libxml2-dev fuse wget cmake
```

The `macOS SDK` can be installed with next script

```shell
cd ./osxcross/tarballs/
wget https://github.com/phracker/MacOSX-SDKs/releases/download/11.3/MacOSX11.3.sdk.tar.xz
cd ..
```

The cross-compiler can be built with next commands

```shell
UNATTENDED=yes OSX_VERSION_MIN=10.7 JOBS=4 ./build.sh
cd ..
```

The final libraries compilation can be performed with command

```shell
./gradlew makeNativeDarwin
```

The compiled files will be places in the next folders:

* `native/bin/Release/darwin` contain macOS native libraries for DFP
* `native/binmath/Release/darwin` contain macOS native libraries for DFP-Math
* `native/binDemo/Release/darwin` contain macOS executable console programs to test how the built programs work in
  different environments

> Note: The native libraries for the `aarch64` platform are not signed, so will not work on ARM-based platforms from
> Apple. If you know how to fix this issue with the CI integration please notify us. The native libraries for the
> `amd64` platform works fine.

#### Generate source wrappers<a name="GenerateSourceWrappers"></a>

The `clang 10` or newer compiler is required for this step.
This step execution was tested on `Ubuntu 20.04`.

To install `clang` compiler execute next command:

```shell
sudo apt install clang
```

The native wrappers are generated with the command

```shell
./gradlew makeNativeWrappers
```

### Native libraries compression<a name="NativeLibrariesCompression"></a>

All the native libraries compiled on the previous step for the `Linux`, `Windows` and `macOS` operating systems must be
copied to the one build host and compressed with Zstandard compressor.

So, the `zstd` with version 1.34 or newer must be installed with next or similar command

```shell
sudo apt install zstd
```

The actual compression can be performed with the next commands

```shell
zstd -19 --rm -r ./native/bin
zstd -19 --rm -r ./native/binmath
```

### Java compilation<a name="JavaCompilation"></a>

As the all previous steps are done, the Java libraries creation can be performed with next command

```shell
./gradlew :java:dfp:jar :java:dfp-math:jar
```

Unit tests can be run as usual with `check`

```shell
./gradlew check
```

The JMH tests can be run with `jmh` command

```shell
./gradlew jmh
```

> Note: There are too many JMH tests there. So, it will take a long time to run them all. Consider selecting just tests
> what you need and locally delete the rest.

### .NET compilation<a name="NetCompilation"></a>

The [Cake](https://cakebuild.net/) build system is used for .NET libraries' compilation, but `Java8` or newer can be
need for some compilation steps.

Next software are required for compilation:

- Build Tools for Visual Studio 2019 (as a separate package or as a part of Visual Studio 2019).
  Note Microsoft Visual 2022 have drop the .NET Framework 4.0 support, so you need or Visual Studio 2019 or tweak your
  Visual Studio by hand
- .NET SDK with `.NET Framework 4.0`, `.NET Standard 2.0` and `.NET Core 3.1` support

The .NET libraries compilation can be performed with the command

```shell
./csharp/build --target=Build
```

The unit tests can be run with command

```shell
./csharp/build --target=Run-Unit-Tests
```

The benchmarks are also compiled by `Build` target can be invoked by command

```shell
dotnet ./csharp/EPAM.Deltix.DFP.Benchmark/bin/Release/netcoreapp3.1/EPAM.Deltix.DFP.Benchmark.dll
```
