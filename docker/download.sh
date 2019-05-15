#!/bin/sh

set -e

while read sha url; do
    wget -nv "$url"
    echo $sha $(basename "$url")
done < downloads.txt | sha256sum -c -
