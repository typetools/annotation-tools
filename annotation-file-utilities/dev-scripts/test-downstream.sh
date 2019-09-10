#!/bin/sh

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

(cd ${AFU} && ./gradlew assemble)

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git

# checker-framework and its downstream tests
/tmp/plume-scripts/git-clone-related typetools checker-framework
(cd ${CHECKERFRAMEWORK} && checker/bin-devel/build.sh)

(cd ${CHECKERFRAMEWORK}/framework && ../gradlew wholeProgramInferenceTests)

/tmp/plume-scripts/git-clone-related typetools checker-framework-inference
(cd ../checker-framework-inference && . ./.travis-build-without-test.sh)

(cd ../checker-framework-inference && ./gradlew dist && ./gradlew test)
