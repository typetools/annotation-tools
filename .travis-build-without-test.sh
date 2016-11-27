#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

export SHELLOPTS

SLUGOWNER=${TRAVIS_REPO_SLUG%/*}

# jsr308-langtools
if [ -d ../jsr308-langtools ] ; then
    (cd ../jsr308-langtools && hg pull && hg update)
else
    set +e
    echo "Running: hg identify https://bitbucket.org/${SLUGOWNER}/jsr308-langtools &>-"
    hg identify https://bitbucket.org/${SLUGOWNER}/jsr308-langtools &>-
    if [ "$?" -eq 0 ]; then
        echo "Running:  (cd .. && hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools)"
        (cd .. && (hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools || hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools))
        echo "... done: (cd .. && hg clone https://bitbucket.org/${SLUGOWNER}/jsr308-langtools)"
    else
        echo "Running: (cd .. && hg clone https://bitbucket.org/typetools/jsr308-langtools)"
        (cd .. && (hg clone https://bitbucket.org/typetools/jsr308-langtools || hg clone https://bitbucket.org/typetools/jsr308-langtools))
        echo "... done: (cd .. && hg clone https://bitbucket.org/typetools/jsr308-langtools)"
    fi
    set -e
fi
(cd ../jsr308-langtools/ && ./.travis-build-without-test.sh)

## Compile
echo "running \"ant compile\" for annotation-tools"
ant compile
