#!/bin/bash

echo Entering "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in "$(pwd)"

# Fail the whole script if any command fails
set -e

# Print commands as they are executed
set -x

export SHELLOPTS

if [ "$(uname)" = "Darwin" ] ; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(which javac)")")")}
fi
echo "JAVA_HOME=$JAVA_HOME"
export AFU="${AFU:-$(cd annotation-file-utilities && pwd -P)}"

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

## Download Gradle and its dependencies (retry in case of network lossage).
(cd "${AFU}" && \
  TERM=dumb timeout 300 ./gradlew --write-verification-metadata sha256 help --dry-run </dev/null >/dev/null 2>&1 || \
  TERM=dumb ./gradlew --write-verification-metadata sha256 help --dry-run </dev/null >/dev/null 2>&1 || \
  (sleep 1m && ./gradlew --write-verification-metadata sha256 help --dry-run))

## Compile
(cd "${AFU}" && ./gradlew assemble)

echo Exiting "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in "$(pwd)"
