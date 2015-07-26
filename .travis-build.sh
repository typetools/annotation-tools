#!/bin/bash
cd $TRAVIS_BUILD_DIR/..
git clone https://github.com/typetools/jsr308-langtools.git
cd jsr308-langtools/make
ant
cd $TRAVIS_BUILD_DIR
pwd
ant clean
ant all
