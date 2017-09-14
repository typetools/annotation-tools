#!/bin/bash

# Fail the whole script if any command fails
set -e

export SHELLOPTS

# For debugging
ant -d compile-scene-lib

./.travis-build-without-test.sh

# For debugging
ant -d compile-scene-lib

ant all

ant check-style

# TODO: when codebase is reformatted (after merging branches?)
# ant check-format

ant html-validate
