#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export AFU=`readlink -f ${AFU:-$(dirname $0)/..}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

./gradlew assemble

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `/tmp/plume-scripts/ci-info typetools`

./gradlew checkBasicStyle
# TODO: enable check-format when codebase is reformatted (after merging branches?)
# ant check-format
./gradlew htmlValidate
./gradlew javadoc
# TODO: check that all changed lines have Javadoc.
