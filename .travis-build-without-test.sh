#!/bin/bash

echo Entering `readlink -f "$0"`

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
echo JAVA_HOME=$JAVA_HOME
export AFU=`readlink -f ${AFU:-annotation-file-utilities}`

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

## Compile
(cd ${AFU} && ./gradlew assemble)

echo Exiting `readlink -f "$0"`
