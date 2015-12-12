#!/bin/bash

# Fail the whole script if any command fails
set -e

./.travis-build-without-test.sh

ant all
