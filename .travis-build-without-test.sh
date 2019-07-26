#!/bin/bash

echo Entering `pwd`/.travis-build-without-test.sh

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}

export AFU=`readlink -f ${AFU:-../annotation-tools/annotation-file-utilities}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

# jsr308-langtools
if [ -d ../jsr308-langtools ] ; then
    (cd ../jsr308-langtools && hg pull && hg update)
else
    set +e
    echo "Running: hg identify https://bitbucket.org/${SLUGOWNER}/jsr308-langtools"
    hg identify https://bitbucket.org/${SLUGOWNER}/jsr308-langtools &>/dev/null
    if [ "$?" -ne 0 ]; then
        SLUGOWNER=typetools
    fi
    set -e
    echo "Running:  (cd .. && hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools)"
    (cd .. && (hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools || hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools))
    echo "... done: (cd .. && hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools)"
fi
(cd ../jsr308-langtools/ && ./.travis-build-without-test.sh)


## Compile
echo "About to run \"ant compile\" for annotation-tools" in `pwd`
which ant
ant compile
echo "Finished running \"ant compile\" for annotation-tools"

echo Exiting `pwd`/.travis-build-without-test.sh
