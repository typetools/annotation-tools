#!/bin/sh

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export AFU=`readlink -f ${AFU:-$(dirname $0)/..}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

ant compile

ant test
