#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export AFU=`readlink -f ${AFU:-$(dirname $0)/..}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

(cd ${AFU} && ./gradlew assemble)

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git

/tmp/plume-scripts/git-clone-related typetools checker-framework ${CHECKERFRAMEWORK}
(cd ${CHECKERFRAMEWORK} && checker/bin-devel/build.sh)

(cd ${AFU} && ./gradlew checkSignature)
