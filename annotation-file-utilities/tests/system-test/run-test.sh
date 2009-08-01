#!/bin/bash
# System test of annotation tools

set -e
trap "echo 'UNEXPECTED ERROR!'" ERR
cd $(dirname $0)

OK=true
function problem {
	OK=
	echo "$1" 1>&2
}


ANNCAT=../../../scene-lib/anncat
XJAVAC="../../../../jsr308-langtools/dist/bin/javac -g"

./clean.sh

# Step 1: Compile the source with annotations.
mkdir out1
$XJAVAC -d out1 source-ann/AnnotationTest.java || problem "TEST 1: ERROR"

# Step 2: Convert the annotated class file to an annotation file.
# Do we get the right annotations?
$ANNCAT --class out1/annotations/tests/AnnotationTest.class --out --index out2.jann || problem "TEST 2: ERROR"
diff -u expected-annos.jann out2.jann | tee out2.diff || problem "TEST 2: FAILURE"

# Step 3: Compile the source without annotations.
mkdir out3
$XJAVAC -d out3 source-plain/AnnotationTest.java || problem "TEST 3: ERROR"

# Step 4: Insert annotations into the class file.
$ANNCAT --index expected-annos.jann --out --class out3/annotations/tests/AnnotationTest.class --to out4.class || problem "TEST 4: ERROR"

# Step 5: Convert the annotation-inserted class file to an annotation file.
# Do we get the right annotations?
# (The annotation-compiled and annotation-inserted class files tend to differ
# for stupid reasons (e.g., order of items in the constant pool), so we don't
# compare them.)
$ANNCAT --class out4.class --out --index out5.jann || problem "TEST 5: ERROR"
diff -u expected-annos.jann out5.jann | tee out5.diff || problem "TEST 5: FAILURE"

if [ $OK ]; then
	echo "All tests succeeded."
else
	echo "TEST(S) FAILED!"
	exit 1
fi
