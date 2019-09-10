#!/bin/bash

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

if [[ "$OSTYPE" == "darwin"* ]]; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}
fi
export AFU="$(cd "${AFU:-annotation-file-utilities}" && pwd -P)"
export CHECKERFRAMEWORK="$(cd "${CHECKERFRAMEWORK:-../checker-framework}" && pwd -P)"
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

(cd ${AFU} && ./gradlew allTests)
