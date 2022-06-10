#!/usr/bin/env bash
set -Eeuo pipefail
set -x

CCOMPILER=${2:-clang}
CXXCOMPILER=${2:-clang}
VERBOSE=${3:-ON}

rm -rf ./build
mkdir build
cd build

$CCOMPILER --version

cmake -G "Unix Makefiles" -DVERSION_SUFFIX=$1 -DCMAKE_VERBOSE_MAKEFILE=$VERBOSE -DCMAKE_C_COMPILER=$CCOMPILER -DCMAKE_CXX_COMPILER=$CXXCOMPILER -DTARGET=arm-linux-gnueabihf -DTARGET_EX='-march=armv7a -mfloat-abi=hard' -DSYSROOT=../../../llvm/clang+llvm-12.0.0-armv7a-linux-gnueabihf -DCMAKE_INSTALL_PREFIX=../ -DINSTALL_SUFFIX=linux/arm ../
make install

rm -rf ./*
cmake -G "Unix Makefiles" -DVERSION_SUFFIX=$1 -DCMAKE_VERBOSE_MAKEFILE=$VERBOSE -DCMAKE_C_COMPILER=$CCOMPILER -DCMAKE_CXX_COMPILER=$CXXCOMPILER -DTARGET=aarch64-linux-gnu -DSYSROOT=../../../llvm/clang+llvm-12.0.0-aarch64-linux-gnu -DCMAKE_INSTALL_PREFIX=../ -DINSTALL_SUFFIX=linux/aarch64 ../
make install

rm -rf ./*
cmake -G "Unix Makefiles" -DVERSION_SUFFIX=$1 -DCMAKE_VERBOSE_MAKEFILE=$VERBOSE -DCMAKE_C_COMPILER=$CCOMPILER -DCMAKE_CXX_COMPILER=$CXXCOMPILER -DTARGET=i686-linux-gnu -DSYSROOT=../../../llvm/clang+llvm-12.0.0-i386-unknown-freebsd12 -DCMAKE_INSTALL_PREFIX=../ -DINSTALL_SUFFIX=linux/i386 ../
make install

rm -rf ./*
cmake -G "Unix Makefiles" -DVERSION_SUFFIX=$1 -DCMAKE_VERBOSE_MAKEFILE=$VERBOSE -DCMAKE_C_COMPILER=musl-gcc -DCMAKE_CXX_COMPILER=musl-gcc -DCMAKE_C_FLAGS_IN=-static -DCMAKE_INSTALL_PREFIX=../ -DINSTALL_SUFFIX=linux/amd64 ../
make install

cd ..
rm -rf ./build
