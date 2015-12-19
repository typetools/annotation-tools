#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

# jsr308-langtools
(cd $ROOT && ((cd jsr308-langtools && hg pull && hg update) || hg clone https://bitbucket.org/typetools/jsr308-langtools))
(cd $ROOT/jsr308-langtools/ && ./.travis-build-without-test.sh)

ant compile
