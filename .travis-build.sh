#!/bin/bash

# Fail the whole script if any command fails
set -e

export SHELLOPTS

./.travis-build-without-test.sh

ant all

ant check-style

# TODO: when codebase is reformatted
# ant check-format

# TODO: When we require Java 8 for compilation:
# ant html-validate
