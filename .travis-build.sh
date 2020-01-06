#!/bin/bash

echo Entering "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in `pwd`

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

if [ -d "/tmp/plume-scripts" ] ; then
  (cd /tmp/plume-scripts && git pull -q) > /dev/null 2>&1
else
  (cd /tmp && git clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git)
fi

if [[ "${GROUP}" == "test" || "${GROUP}" == "all" ]]; then
  (cd annotation-file-utilities && ./gradlew allTests)
fi

if [[ "${GROUP}" == "typecheck" || "${GROUP}" == "all" ]]; then
  if [ -z ${CHECKERFRAMEWORK} ] ; then
    export CHECKERFRAMEWORK=`realpath ../checker-framework`
    /tmp/plume-scripts/git-clone-related typetools checker-framework ${CHECKERFRAMEWORK}
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
    /tmp/plume-scripts/git-clone-related typetools checker-framework
    /tmp/plume-scripts/git-clone-related typetools checker-framework-inference

    (cd ../checker-framework-inference && ./.travis-build.sh)

    (cd ../checker-framework/framework && ../gradlew wholeProgramInferenceTests)
fi

echo Exiting "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in `pwd`
