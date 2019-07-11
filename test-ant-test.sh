#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export PATH=$(dirname $0)/scripts:$JAVA_HOME/bin:$PATH

ant compile

ant test
