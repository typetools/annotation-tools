#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

export SHELLOPTS

# jsr308-langtools
if [ -d ../jsr308-langtools ] ; then
    (cd ../jsr308-langtools && hg pull && hg update)
else
    (cd .. && (hg clone https://bitbucket.org/typetools/jsr308-langtools || hg clone https://bitbucket.org/typetools/jsr308-langtools))
fi
(cd ../jsr308-langtools/ && ./.travis-build-without-test.sh)

ant compile
