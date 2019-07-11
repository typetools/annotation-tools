#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export PATH=$(dirname $0)/../scripts:$JAVA_HOME/bin:$PATH

ant compile

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `/tmp/plume-scripts/ci-info typetools`

ant check-style
# TODO: enable check-format when codebase is reformatted (after merging branches?)
# ant check-format
ant html-validate
ant javadoc
# TODO: check that all changed lines have Javadoc.
