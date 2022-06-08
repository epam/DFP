#!/usr/bin/env bash
set -ex

# https://github.com/tpoechtrager/osxcross
# https://github.com/phracker/MacOSX-SDKs/releases
# https://github.com/ribasco/ucgdisplay/tree/master/native
# https://gist.github.com/jeffheifetz/f9ec3b04312387d6b0720ce41921fddb


OSXCROSS_HOST_SUFFIX=${2:-apple-darwin20.4}
OSXCROSS_TARGET_DIR=${3:-../../osxcross/target}
OSXCROSS_SDK=${4:-$OSXCROSS_TARGET_DIR/SDK/MacOSX11.3.sdk}
OSXCROSS_TARGET=${5:-darwin20.4}
VERBOSE=${6:-ON}

rm -rf ./build
mkdir build
cd build

CROSS_HOST=x86_64
OSXCROSS_HOST=$CROSS_HOST-$OSXCROSS_HOST_SUFFIX \
	OSXCROSS_TARGET_DIR=$OSXCROSS_TARGET_DIR \
	OSXCROSS_SDK=$OSXCROSS_SDK \
	OSXCROSS_TARGET=$OSXCROSS_TARGET \
	cmake -G "Unix Makefiles" -DVERSION_SUFFIX=$1 -DCMAKE_VERBOSE_MAKEFILE=$VERBOSE -DCMAKE_TOOLCHAIN_FILE=$OSXCROSS_TARGET_DIR/toolchain.cmake -DCMAKE_INSTALL_PREFIX=../ -DINSTALL_SUFFIX=darwin/amd64 -DAPPLE=TRUE ../
make install

rm -rf ./*
CROSS_HOST=aarch64
OSXCROSS_HOST=$CROSS_HOST-$OSXCROSS_HOST_SUFFIX \
	OSXCROSS_TARGET_DIR=$OSXCROSS_TARGET_DIR \
	OSXCROSS_SDK=$OSXCROSS_SDK \
	OSXCROSS_TARGET=$OSXCROSS_TARGET \
	cmake -G "Unix Makefiles" -DVERSION_SUFFIX=$1 -DCMAKE_VERBOSE_MAKEFILE=$VERBOSE -DCMAKE_TOOLCHAIN_FILE=$OSXCROSS_TARGET_DIR/toolchain.cmake -DCMAKE_INSTALL_PREFIX=../ -DINSTALL_SUFFIX=darwin/aarch64 -DAPPLE=TRUE ../
make install

cd ..
rm -rf ./build
