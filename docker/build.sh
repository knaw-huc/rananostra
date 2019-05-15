#!/bin/sh

set -e

# Ignore warnings about OpenMP pragmas.
export CFLAGS=-Wno-unknown-pragmas
export CXXFLAGS=-Wno-unknown-pragmas

while read sha url; do
    tarball=$(basename "$url")
    tar xzf "$tarball"
    cd $(basename "$tarball" .tar.gz)
    ./configure --disable-openmp --disable-shared
    make -j $(nproc)
    make install
    cd ..
done < downloads.txt
