#!/bin/bash

echo Entering "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in `pwd`

# Fail the whole script if any command fails
set -e

export SHELLOPTS

if [ "$(uname)" = "Darwin" ] ; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}
fi
echo JAVA_HOME=$JAVA_HOME
export AFU="${AFU:-$(cd annotation-file-utilities && pwd -P)}"

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

## Download Gradle itself if necessary (retry for network lossage)
(cd ${AFU} && timeout 300 ./gradlew tasks || ./gradlew tasks)

## Compile
(cd ${AFU} && ./gradlew assemble)

echo Exiting "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in `pwd`
