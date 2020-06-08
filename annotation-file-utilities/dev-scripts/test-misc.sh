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
export AFU="${AFU:-$(cd annotation-file-utilities >/dev/null 2>&1 && pwd -P)}"
export CHECKERFRAMEWORK="${CHECKERFRAMEWORK:-$(cd .. >/dev/null 2>&1 && pwd -P)/checker-framework}"
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

if [ -d "/tmp/$USER/plume-scripts" ] ; then
  (cd "/tmp/$USER/plume-scripts" && git pull -q) > /dev/null 2>&1
else
  mkdir -p "/tmp/$USER" && git -C "/tmp/$USER" clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
fi

cd "${AFU}"
./gradlew assemble

./gradlew checkBasicStyle
# TODO: enable check-format when codebase is reformatted (after merging branches?)
# ant check-format

./gradlew htmlValidate

./gradlew javadoc
(./gradlew javadocPrivate > /tmp/warnings.txt 2>&1) || true
"/tmp/$USER/plume-scripts/ci-lint-diff" /tmp/warnings.txt
(./gradlew requireJavadoc > /tmp/warnings.txt 2>&1) || true
"/tmp/$USER/plume-scripts/ci-lint-diff" /tmp/warnings.txt
