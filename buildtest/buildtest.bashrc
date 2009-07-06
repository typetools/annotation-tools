# Minimal bash (does not work with sh) startup file to ensure the
# annotations/javari projects only read the recently checked out projects.

export PAG_DAIKON_DIR=/afs/csail/group/pag/software/bin
if [ ! -d ${PAG_DAIKON_DIR} ]; then
  echo "exiting buildtest.bashrc: can't find directory ${PAG_DAIKON_DIR}"
  return 2
fi
# This permits pag-daikon.bashrc to be found if one isn't running with
# access to AFS.  However, there is no point in doing this unless other
# scripts in the buildtest system are modified to work without access to AFS.
if [ ! -d ${PAG_DAIKON_DIR} ]; then
  export PAG_DAIKON_DIR=${HOME}/research/invariants/scripts
  if [ ! -d ${PAG_DAIKON_DIR} ]; then
    echo "exiting buildtest.bashrc: can't find directory containing pag-daikon.bashrc"
    return 2
  fi
fi

source ${PAG_DAIKON_DIR}/pag-daikon.bashrc

# Need to unset this variable so jsr308 compiles
unset JAVA_HOME

ant init
# Checking out the projects creates a directory of the form: 
#  bulid-current-<datestamp> to use as the workspace.
export JAVARI_ANNOTATION_WORKSPACE=`echo $PWD`/`find . -name 'build-current*'`
export ASMX=${JAVARI_ANNOTATION_WORKSPACE}/annotations/asmx
export ANNOTATED_JDK=${JAVARI_ANNOTATION_WORKSPACE}/qualifiers/annotated-jdk
export JAVARI_BUILTINS=${JAVARI_ANNOTATION_WORKSPACE}/javari/builtins
export ANNOTATION_SCENE_LIB=${JAVARI_ANNOTATION_WORKSPACE}/annotations/scene-lib
export NONNULL_BUILTINS=${JAVARI_ANNOTATION_WORKSPACE}/nonnull/builtins
export JAVARIFIER=${JAVARI_ANNOTATION_WORKSPACE}/javari/javarifier

# TODO: Using a safe version of the annotated JDK is a temporary solution that
# allows you to use a precompiled verison of the JDK classes as stubs to
# Javarifier.  This workaround is necessary since the current jsr308 compiler
# has a bug that sometimes produces bad class files.  Once that bug is fixed,
# this version of the JDK should not be used and below, all instances of
# SAFE_ANNOTATED_JDK should be replaced with ANNOTATED_JDK
export SAFE_ANNOTATED_JDK=/afs/csail.mit.edu/u/j/jaimeq/research/qualifiers/annotated-jdk

# Javarifer needs access to these classes.
export JRE1_6_0=/afs/csail/group/pag/software/pkg/jdk/jre/lib/rt.jar
export JASMINCLASSES_2_2_3=/afs/csail.mit.edu/u/j/jaimeq/soot2/jasminclasses-2.2.3.jar
export POLYGLOTCLASSES_1_3_2=/afs/csail.mit.edu/u/j/jaimeq/soot2/polyglotclasses-1.3.2.jar
export SOOTCLASSES_2_2_3=/afs/csail.mit.edu/u/j/jaimeq/soot2/sootclasses-2.2.3.jar
export SOOT_STUBS=${SAFE_ANNOTATED_JDK}/bin:$JAVARI_BUILTINS/bin:$NONNULL_BUILTINS/bin
export SOOT_WORLD=$JRE1_6_0
export CLASSPATH=${JASMINCLASSES_2_2_3}:${POLYGLOTCLASSES_1_3_2}:${SOOTCLASSES_2_2_3}:${CLASSPATH}

export CLASSPATH=${ASMX}/bin:${ANNOTATION_SCENE_LIB}/bin:${SAFE_ANNOTATED_JDK}/bin:${JAVARI_BUILTINS}/bin:${NONNULL_BUILTINS}/bin:${JAVARIFIER}/bin:${SAFE_ANNOTATED_JDK}/bin:${CLASSPATH}


alias javarify='$JAVARIFIER/javarify.sh .:./bin $SOOT_STUBS $SOOT_WORLD '
