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

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git

cd ${AFU}
./gradlew assemble

./gradlew checkBasicStyle
# TODO: enable check-format when codebase is reformatted (after merging branches?)
# ant check-format

./gradlew htmlValidate

./gradlew javadoc
(./gradlew javadocPrivate > /tmp/warnings.txt 2>&1) || true
/tmp/plume-scripts/ci-lint-diff /tmp/warnings.txt
(./gradlew requireJavadocPrivate > /tmp/warnings.txt 2>&1) || true
/tmp/plume-scripts/ci-lint-diff /tmp/warnings.txt
