#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..
git clone https://github.com/typetools/jsr308-langtools.git
cd jsr308-langtools/
./.travis-build.sh

cd $ROOT/annotation-tools
ant clean
ant all
