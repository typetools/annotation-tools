#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..
cd $ROOT
git clone https://github.com/typetools/jsr308-langtools.git
cd jsr308-langtools/
./.travis-build.sh

cd $ROOT/annotation-tools
ant clean
ant all

echo Triggering build of typetools/checker-framework
curl -s https://raw.githubusercontent.com/mernst/plume-lib/master/bin/trigger-travis.sh > trigger-travis.sh
bash trigger-travis.sh typetools checker-framework $TRAVISTOKEN
rm trigger-travis.sh
