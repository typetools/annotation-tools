#!/bin/bash
cd $TRAVIS_BUILD_DIR/..
git clone https://github.com/typetools/jsr308-langtools.git
cd jsr308-langtools/
./.travis-build.sh

cd $TRAVIS_BUILD_DIR
ant clean
ant all
