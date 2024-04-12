#!/usr/bin/env bash
set -e

mkdir build
cd build
cmake -G "Unix Makefiles" -DCMAKE_C_COMPILER=clang-9 ../
make VERBOSE=TRUE
