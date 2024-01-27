#!/bin/bash
# Uses `bash` rather than `sh` because of use of [[ ... ]] for boolean tests.

echo "Entering $(cd "$(dirname "$0")" && pwd -P)/$(basename "$0") in $(pwd)"

# Optional argument $1 is one of:
#   all, test, typecheck, misc, downstream
# It defaults to "all".
export GROUP="$1"
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

./.build-without-test.sh

set -e


if [ -d "/tmp/git-scripts" ] ; then
  (cd /tmp/git-scripts && (git pull -q || true)) > /dev/null 2>&1
else
  (cd /tmp && git clone --filter=blob:none -q https://github.com/plume-lib/git-scripts.git)
fi

if [[ "${GROUP}" == "test" || "${GROUP}" == "all" ]]; then
  (cd annotation-file-utilities && ./gradlew build)
fi

if [[ "${GROUP}" == "typecheck" || "${GROUP}" == "all" ]]; then
  if [ -z "${CHECKERFRAMEWORK}" ] ; then
    CHECKERFRAMEWORK=$(realpath ../checker-framework)
    export CHECKERFRAMEWORK
    /tmp/git-scripts/git-clone-related typetools checker-framework "${CHECKERFRAMEWORK}"
    (cd "${CHECKERFRAMEWORK}" && ./.build-without-test.sh downloadjdk)
  fi

  (cd annotation-file-utilities && ./gradlew checkSignature)
fi

if [[ "${GROUP}" == "misc" || "${GROUP}" == "all" ]]; then
  ## jdkany tests: miscellaneous tests that shouldn't depend on JDK version.

  set -e

  # As of version 2.38.0 (2023-04-06), Spotless does not run under JDK 20.
  JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1 | sed 's/-ea//') && \
  if [ "$JAVA_VER" = "20" ] ; then
     echo "Skipping spotlessCheck on JDK 20"
  else
    (cd annotation-file-utilities && ./gradlew spotlessCheck)
  fi

  (cd annotation-file-utilities && ./gradlew htmlValidate)

  (cd annotation-file-utilities && ./gradlew javadoc)
fi

if [[ "${GROUP}" == "downstream" || "${GROUP}" == "all" ]]; then
    # checker-framework and its downstream tests
    /tmp/git-scripts/git-clone-related typetools checker-framework
    (cd ../checker-framework/framework && (../gradlew --write-verification-metadata sha256 help --dry-run || (sleep 60s && ../gradlew --write-verification-metadata sha256 help --dry-run)))
    (cd ../checker-framework/framework && ../gradlew ainferTest)

    # /tmp/git-scripts/git-clone-related typetools checker-framework-inference
    # (cd ../checker-framework-inference && ./.build.sh)
fi

echo "Exiting $(cd "$(dirname "$0")" && pwd -P)/$(basename "$0") in $(pwd)"
