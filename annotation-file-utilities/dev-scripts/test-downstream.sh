#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

if [ "$(uname)" = "Darwin" ] ; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}
fi
export AFU="${AFU:-$(cd annotation-file-utilities && pwd -P)}"
export CHECKERFRAMEWORK="${CHECKERFRAMEWORK:-$(pwd -P)/../checker-framework}"
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

(cd ${AFU} && ./gradlew assemble)

if [ -d "/tmp/plume-scripts" ] ; then
  (cd /tmp/plume-scripts && git pull -q) > /dev/null 2>&1
else
  (cd /tmp && git clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git)
fi

# checker-framework and its downstream tests
/tmp/plume-scripts/git-clone-related typetools checker-framework ${CHECKERFRAMEWORK}
(cd ${CHECKERFRAMEWORK} && checker/bin-devel/build.sh)

(cd ${CHECKERFRAMEWORK}/framework && ../gradlew wholeProgramInferenceTests)

/tmp/plume-scripts/git-clone-related typetools checker-framework-inference
(cd ../checker-framework-inference && . ./.travis-build-without-test.sh)

(cd ../checker-framework-inference && ./gradlew dist && ./gradlew test)
