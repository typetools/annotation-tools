#!/bin/bash

echo Entering `pwd`/.travis-build-without-test.sh

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}

export AFU=`readlink -f ${AFU:-../annotation-tools/annotation-file-utilities}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
    || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `ci-info`

set -e

## Compile
echo "About to run \"ant compile\" for annotation-tools" in `pwd`
which ant
ant compile
echo "Finished running \"ant compile\" for annotation-tools"

echo Exiting `pwd`/.travis-build-without-test.sh
