#!/bin/bash
cd $HOME
git clone https://github.com/typetools/jsr308-langtools.git
cd jsr308-langtools/make
ant
cd $TRAVIS_BUILD_DIR
pwd
ant clean
ant -Dannotations-compiler=$HOME/jsr308-langtools -Dannotations-disassembler=$HOME/jsr308-langtools all
