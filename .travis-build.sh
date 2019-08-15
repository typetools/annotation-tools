#!/bin/bash

echo Entering `pwd`/.travis-build.sh, GROUP=$1

# Optional argument $1 is one of:
#   all, test, typecheck, misc, downstream
# It defaults to "all".
export GROUP=$1
if [[ "${GROUP}" == "" ]]; then
  export GROUP=all
fi

if [[ "${GROUP}" != "all" && "${GROUP}" != "test" && "${GROUP}" != "typecheck" && "${GROUP}" != "misc" && "${GROUP}" != "downstream" ]]; then
  echo "Bad argument '${GROUP}'; should be omitted or one of: all, test, misc, downstream."
  exit 1
fi

# Fail the whole script if any command fails
set -e


## Diagnostic output
# Output lines of this script as they are read.
set -o verbose
# Output expanded lines of this script as they are executed.
set -o xtrace
# Don't use "-d" to debug ant, because that results in a log so long
# that Travis truncates the log and terminates the job.

export SHELLOPTS

./.travis-build-without-test.sh

set -e

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `/tmp/plume-scripts/ci-info typetools`

if [[ "${GROUP}" == "test" || "${GROUP}" == "all" ]]; then
  (cd annotation-file-utilities && ./gradlew allTests)
fi

if [[ "${GROUP}" == "typecheck" || "${GROUP}" == "all" ]]; then
  if [ -z ${CHECKERFRAMEWORK} ] ; then
    (cd .. && git clone --depth 1 https://github.com/plume-lib/plume-scripts.git)
    REPO=`../plume-scripts/git-find-fork ${CI_ORGANIZATION} typetools checker-framework`
    BRANCH=`../plume-scripts/git-find-branch ${REPO} ${CI_BRANCH}`
    (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO}) || (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 ${REPO})
    export CHECKERFRAMEWORK=`realpath ../checker-framework`
    (cd ${CHECKERFRAMEWORK} && ./.travis-build-without-test.sh downloadjdk)
  fi

  (cd annotation-file-utilities && ./gradlew checkSignature)
fi

if [[ "${GROUP}" == "misc" || "${GROUP}" == "all" ]]; then
  ## jdkany tests: miscellaneous tests that shouldn't depend on JDK version.

  set -e

  (cd annotation-file-utilities && ./gradlew checkBasicStyle)

  # TODO: when codebase is reformatted (after merging branches?)
  # ant check-format

  (cd annotation-file-utilities && ./gradlew htmlValidate)

  (cd annotation-file-utilities && ./gradlew javadoc)
fi

if [[ "${GROUP}" == "downstream" || "${GROUP}" == "all" ]]; then
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
fi

echo Exiting `pwd`/.travis-build.sh, GROUP=$1
