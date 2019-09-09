#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export AFU=`readlink -f ${AFU:-$(dirname $0)/..}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

cd ${AFU}
./gradlew assemble

./gradlew checkBasicStyle
# TODO: enable check-format when codebase is reformatted (after merging branches?)
# ant check-format
./gradlew htmlValidate
./gradlew javadoc
# TODO: check that all changed lines have Javadoc.
