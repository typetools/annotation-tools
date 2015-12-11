#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# jsr308-langtools
(cd $ROOT && hg clone https://bitbucket.org/typetools/jsr308-langtools)
(cd $ROOT/jsr308-langtools/ && ./.travis-build-without-test.sh)

ant compile
