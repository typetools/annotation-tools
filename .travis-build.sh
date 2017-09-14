#!/bin/bash

# Fail the whole script if any command fails
set -e

export SHELLOPTS

./.travis-build-without-test.sh

ant all

ant check-style

# TODO: when codebase is reformatted (after merging branches?)
# ant check-format

ant html-validate
