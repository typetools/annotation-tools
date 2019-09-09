#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}

export AFU=`readlink -f ${AFU:-annotation-file-utilities}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

(cd ${AFU} && ./gradlew assemble)

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `/tmp/plume-scripts/ci-info typetools`

# checker-framework and its downstream tests
(cd .. && git clone --depth 1 https://github.com/plume-lib/plume-scripts.git)
REPO=`../plume-scripts/git-find-fork ${CI_ORGANIZATION} typetools checker-framework`
BRANCH=`../plume-scripts/git-find-branch ${REPO} ${CI_BRANCH}`
(cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO}) || (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO})
REPO=`../plume-scripts/git-find-fork ${CI_ORGANIZATION} typetools checker-framework-inference`
BRANCH=`../plume-scripts/git-find-branch ${REPO} ${CI_BRANCH}`
(cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO}) || (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO})

(cd ../checker-framework-inference && . ./.travis-build-without-test.sh)

(cd ../checker-framework/framework && ../gradlew wholeProgramInferenceTests)
(cd ../checker-framework-inference && ./gradlew dist && ./gradlew test)
