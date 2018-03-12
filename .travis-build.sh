#!/bin/bash

# Optional argument $1 is one of:
#   all, test, misc
# If it is omitted, this script does everything.
export GROUP=$1
if [[ "${GROUP}" == "" ]]; then
  export GROUP=all
fi

if [[ "${GROUP}" != "all" && "${GROUP}" != "test" && "${GROUP}" != "misc" && "${GROUP}" != "downstream" ]]; then
  echo "Bad argument '${GROUP}'; should be omitted or one of: all, test, misc."
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
  SLUGOWNER=typetools
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
    set +e
    echo "Running: git ls-remote https://github.com/${SLUGOWNER}/checker-framework-inference.git &>-"
    git ls-remote https://github.com/${SLUGOWNER}/checker-framework-inference.git &>-
    if [ "$?" -ne 0 ]; then
        CFISLUGOWNER=typetools
    else
        CFISLUGOWNER=${SLUGOWNER}
    fi
    set -e
    echo "Running:  (cd .. && git clone --depth 1 https://github.com/${CFISLUGOWNER}/checker-framework-inference.git)"
    (cd .. && git clone --depth 1 https://github.com/${CFISLUGOWNER}/checker-framework-inference.git) || (cd .. && git clone --depth 1 https://github.com/${CFISLUGOWNER}/checker-framework-inference.git)
    echo "... done: (cd .. && git clone --depth 1 https://github.com/${CFISLUGOWNER}/checker-framework-inference.git)"

    cd ../checker-framework-inference
    . ./.travis-build-without-test.sh

    (cd ../checker-framework/framework && ant whole-program-inference-tests)
    (cd ../checker-framework-inference && ant -f tests.xml run-tests)
fi
