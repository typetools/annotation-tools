#!/bin/bash

echo "Entering annotation-tools/.travis-build-without-test.sh"

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}

export JSR308=`readlink -f ${JSR308:-..}`
export AFU=`readlink -f ${AFU:-../annotation-tools/annotation-file-utilities}`
export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-../checker-framework}`

export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH


SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=eisop
fi


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
echo "running \"ant compile\" for annotation-tools"
pwd
ant compile
echo "done running \"ant compile\" for annotation-tools"

echo "Exiting annotation-tools/.travis-build-without-test.sh"
