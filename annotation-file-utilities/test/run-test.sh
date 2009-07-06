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


ANNCAT=../scene-lib/anncat
XJAVAC="../langtools/dist/bin/javac -g"

./clean.sh

# Step 1: Compile the source with annotations.
mkdir tmp1
$XJAVAC -d tmp1 source-ann/AnnotationTest.java || problem "1 ERROR"

# Step 2: Convert the annotated class file to an annotation file.
# Do we get the right annotations?
$ANNCAT --class tmp1/annotations/tests/AnnotationTest.class --out --index tmp2.jann || problem "2 ERROR"
diff -u expected-annos.jann tmp2.jann || problem "2 FAILURE"

# Step 3: Compile the source without annotations.
mkdir tmp3
$XJAVAC -d tmp3 source-plain/AnnotationTest.java || problem "3 ERROR"

# Step 4: Insert annotations into the class file.
$ANNCAT --index expected-annos.jann --out --class tmp3/annotations/tests/AnnotationTest.class --to tmp4.class || problem "4 ERROR"

# Step 5: Convert the annotation-inserted class file to an annotation file.
# Do we get the right annotations?
# (The annotation-compiled and annotation-inserted class files tend to differ
# for stupid reasons (e.g., order of items in the constant pool), so we don't
# compare them.)
$ANNCAT --class tmp4.class --out --index tmp5.jann || problem "5 ERROR"
diff -u expected-annos.jann tmp5.jann || problem "5 FAILURE"

if [ $OK ]; then
	echo "All tests succeeded."
else
	echo "TEST(S) FAILED!"
	exit 1
fi
