#!/bin/bash

echo "Entering annotation-tools/.travis-build.sh"

# Optional argument $1 is one of:
#   all, test, misc, downstream
# If it is omitted, this script does everything.
export GROUP=$1
if [[ "${GROUP}" == "" ]]; then
  export GROUP=all
fi

if [[ "${GROUP}" != "all" && "${GROUP}" != "test" && "${GROUP}" != "misc" && "${GROUP}" != "downstream" ]]; then
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

SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=eisop
fi

./.travis-build-without-test.sh

set -e

if [[ "${GROUP}" == "test" || "${GROUP}" == "all" ]]; then
  ant test
fi

if [[ "${GROUP}" == "misc" || "${GROUP}" == "all" ]]; then
  ## jdkany tests: miscellaneous tests that shouldn't depend on JDK version.

  set -e

  ant check-style

  # TODO: when codebase is reformatted (after merging branches?)
  # ant check-format

  ant html-validate

  ant javadoc
fi

if [[ "${GROUP}" == "downstream" || "${GROUP}" == "all" ]]; then
    # checker-framework and its downstream tests
    (cd .. && git clone --depth 1 https://github.com/plume-lib/plume-scripts.git)
    REPO=`../plume-scripts/git-find-fork ${SLUGOWNER} typetools checker-framework-inference`
    BRANCH=`../plume-scripts/git-find-branch $REPO ${TRAVIS_PULL_REQUEST_BRANCH:-$TRAVIS_BRANCH}`
    (cd .. && git clone -b $BRANCH --single-branch --depth 1 $REPO) || (cd .. && git clone -b $BRANCH --single-branch --depth 1 $REPO)

    (cd ../checker-framework-inference && . ./.travis-build-without-test.sh)

    (cd ../checker-framework/framework && ../gradlew wholeProgramInferenceTests)
    (cd ../checker-framework-inference && ./gradlew dist && ./gradlew test)
fi

echo "Exiting annotation-tools/.travis-build.sh"
