#!/bin/bash

echo Entering `pwd`/.travis-build-without-test.sh

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
echo JAVA_HOME=$JAVA_HOME
export AFU=`readlink -f ${AFU:-../annotation-tools/annotation-file-utilities}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

## Compile
echo "About to run \"ant compile\" for annotation-tools" in `pwd`
which ant
ant compile
echo "Finished running \"ant compile\" for annotation-tools"

echo Exiting `pwd`/.travis-build-without-test.sh
