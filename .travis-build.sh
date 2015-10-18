#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..
cd $ROOT
hg clone https://bitbucket.org/typetools/jsr308-langtools
cd jsr308-langtools/
./.travis-build.sh

cd $ROOT/annotation-tools
ant clean
ant all
