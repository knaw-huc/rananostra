#!/bin/sh

set -e

frog --skip=mptcla -S 9999 -X &
sleep 5     # wait for Frog to start

bin/rananostra server config.yml
