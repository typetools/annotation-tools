#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export AFU=`readlink -f ${AFU:-../annotation-tools/annotation-file-utilities}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

ant compile

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `/tmp/plume-scripts/ci-info typetools`

(cd .. && git clone --depth 1 https://github.com/plume-lib/plume-scripts.git)
REPO=`../plume-scripts/git-find-fork ${CI_ORGANIZATION} typetools checker-framework`
BRANCH=`../plume-scripts/git-find-branch ${REPO} ${CI_BRANCH}`
(cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO}) || (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO})
export CHECKERFRAMEWORK=`realpath ../checker-framework`
(cd ${CHECKERFRAMEWORK} && ./.travis-build-without-test.sh downloadjdk)

(cd annotation-file-utilities && ant check-signature)
(cd scene-lib && ant check-signature)
