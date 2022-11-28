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

The project includes several modules. Run this command to clone the repository with modules:

```git
git clone --recursive https://github.com/epam/DFP.git DFP
```

## Building from source<a name="BuildingFromSource"></a>

> All build steps require `Java8` or newer for execution. The build was tested on `Ubuntu 20.04` and `Windows 10` operating systems and `amd64` platform.

The DFP project has four targets:

* DFP for Java
* DFP-Math for Java
* DFP for .NET
* DFP-Math for .NET
 
All targets, with the exception of DFP for Java, depend on native libraries. Compile and compress native libraries for all the
supported platforms before Java and/or .NET compilation.

> In case of **DFP for Java**, the necessary code was ported from C. To build **only** DFP for Java and run unit tests, all you need is sources in this repository and JDK. 

Let's investigate thoroughly these two build options:

* [Java-only DFP-only build](#JavaOnlyDfpOnlyBuild)
* [Complete build](#CompleteBuild)

## Java-only DFP-only build and test<a name="JavaOnlyDfpOnlyBuild"></a>

> `JDK8` or newer is required for this compilation step.

You can find **Java-only DFP** and unit tests in the `:java:dfp` project.\
To execute all targets in this project, run this command:

```shell
./gradlew :java:dfp:build
```

All dependencies are compiled automatically. You do not need anything other than JDK.

Some DFP tests require native libraries. All native-dependent tests and JMH benchmarks are separated to the `:java:dfpNativeTests` project. 

> Refer to [Complete build](#CompleteBuild), if you want to run all DFP tests or benchmarks.

## Complete build and test<a name="CompleteBuild"></a>

To run the **complete build**, follow these steps:

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

> It is assumed, that this step is executed on `Ubuntu 20.04` or newer on the `amd64` platform.

This steps includes the cross-compilation compilation of Linux native libraries for the `armv7a`, `aarch64`, `i686`, `amd64` platforms.

1. These packages are **required** for the compilation:

    ```shell
    sudo apt install curl tar build-essential cmake g++-9-arm-linux-gnueabihf g++-9-aarch64-linux-gnu g++-9-i686-linux-gnu musl-tools
    ```

2. `clang` cross-compilers are also **required**:

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

3. Perform the compilation:

    ```shell
    ./gradlew makeNativeLinux
    ```

All the compiled files are placed in these folders:

* `native/bin/Release/linux` — contains Linux native libraries for DFP.
* `native/binmath/Release/linux` — contains Linux native libraries for DFP-Math.
* `native/binDemo/Release/linux` — contains Linux executable console programs to test the built programs on different environments.

#### Build Windows native libraries<a name="BuildWindowsNativeLibraries"></a>

> It is assumed, that this step is executed on `Windows 10` or newer on the `amd64` platform.

This steps includes the compilation of Windows native libraries for the `i686` and `amd64` platforms.

1. This software is required for the compilation:
    * Microsoft Visual C++ 2019 Build Tools (as a separate package or as a part of Visual Studio 2019) with `ClangCL` platform toolkit.
    * CMake 3.20 or newer.

2. Run the compilation from the **Developer Command Prompt** when all dependencies are met:

    ```shell
    gradlew makeNativeWindows
    ```

All the compiled files are placed in these folders:

* `native/bin/Release/windows` — contains Windows native libraries for DFP.
* `native/binmath/Release/windows` — contains Windows native libraries for DFP-Math.
* `native/binDemo/Release/windows` — contains Windows executable console programs to test the built programs on different environments.

#### Build macOS native libraries<a name="BuildMacOSNativeLibraries"></a>

> It is assumed, that the `macOS` native libraries are built via cross-compilation on `Ubuntu 20.04` or newer on the `amd64` platform.

1. These packages are required for the compilation:

    ```shell
    sudo apt install clang make libssl-dev liblzma-dev libz-dev libxml2-dev fuse wget cmake
    ```

2. Run this script to install `macOS SDK`:

    ```shell
    cd ./osxcross/tarballs/
    wget https://github.com/phracker/MacOSX-SDKs/releases/download/11.3/MacOSX11.3.sdk.tar.xz
    cd ..
    ```

3. Run this command to build the cross-compiler:

    ```shell
    UNATTENDED=yes OSX_VERSION_MIN=10.7 JOBS=4 ./build.sh
    cd ..
    ```

4. Run this command to compile libraries:

    ```shell
    ./gradlew makeNativeDarwin
    ```

All the compiled files are placed in these folders:

* `native/bin/Release/darwin` — contains macOS native libraries for DFP.
* `native/binmath/Release/darwin` — contains macOS native libraries for DFP-Math.
* `native/binDemo/Release/darwin` — contains macOS executable console programs to test the built programs on different environments.

> Note: The native libraries for the `aarch64` platform are not signed and do not work on ARM-based platforms from
> Apple. If you know how to fix this issue with the CI integration, please notify us. The native libraries for the
> `amd64` platform work well.

#### Generate source wrappers<a name="GenerateSourceWrappers"></a>

> This step requires the compiler `clang 10` or newer.

> The execution of this step was tested on `Ubuntu 20.04`.

1. To install `clang` compiler, run this command:

    ```shell
    sudo apt install clang
    ```

2. Run this command to generate native wrappers:

    ```shell
    ./gradlew makeNativeWrappers
    ```

### Native libraries compression<a name="NativeLibrariesCompression"></a>

All native libraries compiled in previous steps for `Linux`, `Windows`, and `macOS` operating systems must be
copied to one build host and compressed with the **Zstandard** compressor.

1. Install version 1.34 or newer of the `zstd`: 

    ```shell
    sudo apt install zstd
    ```

2. Run this command to perform the compression:

    ```shell
    zstd -19 --rm -r ./native/bin
    zstd -19 --rm -r ./native/binmath
    ```

### Java compilation<a name="JavaCompilation"></a>

1. Run this command to create Java libraries when all previous steps are completed:

    ```shell
    ./gradlew :java:dfp:jar :java:dfp-math:jar
    ```

2. Execute `check` to run unit tests:

    ```shell
    ./gradlew check
    ```

3. Execute `jmh` to run JMH tests:

    ```shell
    ./gradlew jmh
    ```

> Note: Due to the large quantity of JMH tests, it takes a significant amount of time to run them all. We suggest to choose just the ones you need and locally remove the rest.

### .NET compilation<a name="NetCompilation"></a>

> We use [Cake](https://cakebuild.net/) build system to compile .NET libraries, but `Java8` or newer can be required for some compilation steps.

1. This software is required for the compilation:
    * Build Tools for MS Visual Studio 2019 (as a separate package or as a part of Visual Studio 2019). Note, that MS Visual Studio 2022 does not support the .NET Framework 4.0. We recommend using MS Visual Studio 2019 or tweaking your version of MS Visual Studio by hand.
    * .NET SDK with the support of `.NET Framework 4.0`, `.NET Standard 2.0` and `.NET Core 3.1`.
2. Run the compilation of .NET libraries:

    ```shell
    ./csharp/build --target=Build
    ```

3. Execute this command to run unit tests:

    ```shell
    ./csharp/build --target=Run-Unit-Tests
    ```

4. Run this command to invoke the compiled Benchmarks by `Build` target:

    ```shell
    dotnet ./csharp/EPAM.Deltix.DFP.Benchmark/bin/Release/netcoreapp3.1/EPAM.Deltix.DFP.Benchmark.dll
    ```
