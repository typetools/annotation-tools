#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

if [ "$(uname)" = "Darwin" ] ; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(which javac)")")")}
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

status=0

# Code style and formatting
./gradlew checkBasicStyle --console=plain --warning-mode=all --no-daemon || status=1
# TODO: enable check-format when codebase is reformatted (after merging branches?)
# ant check-format || status=1

# HTML legality
./gradlew htmlValidate --console=plain --warning-mode=all --no-daemon || status=1

# Javadoc documentation
./gradlew javadoc --console=plain --warning-mode=all --no-daemon || status=1
(./gradlew javadocPrivate --console=plain --warning-mode=all --no-daemon > /tmp/warnings.txt 2>&1) || true
"/tmp/$USER/plume-scripts/ci-lint-diff" /tmp/warnings.txt || status=1
# For refactorings that touch a lot of code that you don't understand, create
# top-level file SKIP-REQUIRE-JAVADOC.  Delete it after the pull request is merged.
if [ ! -f SKIP-REQUIRE-JAVADOC ]; then
  (./gradlew requireJavadoc --console=plain --warning-mode=all --no-daemon > /tmp/warnings-rjp.txt 2>&1) || true
  /tmp/"$USER"/plume-scripts/ci-lint-diff /tmp/warnings-rjp.txt || status=1
  (./gradlew javadocDoclintAll --console=plain --warning-mode=all --no-daemon > /tmp/warnings-jda.txt 2>&1) || true
  /tmp/"$USER"/plume-scripts/ci-lint-diff /tmp/warnings-jda.txt || status=1
fi
if [ $status -ne 0 ]; then exit $status; fi
