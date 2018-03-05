#!/bin/bash

# Fail the whole script if any command fails
set -e

export SHELLOPTS

SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=typetools
fi

## Compile
echo "running \"ant compile\" for annotation-tools"
pwd
ant compile
echo "done running \"ant compile\" for annotation-tools"
