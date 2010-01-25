# Very rough testing framework for the annotator that is based on the
# testing framework for the Javarifier.  Running 'make all' will look for
# all myClass.goal files in this directory, run the annotator on the
# corresponding .jaif and .java files, and then output the difference in a
# myClass.diff file in this directory.
#
# To test just one file, use (for example) 'make myClass.diff'.

export SHELL=/bin/bash -o pipefail

DIFFS := $(wildcard *.goal)
DIFFS := $(patsubst %.goal, %.diff, $(DIFFS))

DEBUG :=
# Use this to enable some debugging.
# DEBUG := --debug

default : all

.PHONY: all
all : $(DIFFS) system-test results

.PHONY: system-test
system-test:
	${MAKE} -C system-test

# Display results of all .diff files.
.PHONY: results
results: bin/VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@java -cp bin VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
bin/VerifyDiffs.class : VerifyDiffs.java
	@javac -g -cp ../bincompile -d bin VerifyDiffs.java

# Compiles all the test cases (be verbose about this).
compile :
	mkdir -p bin
	javac -g -cp ../bin -d bin *.java

.PRECIOUS : bin/annotator/tests/%.class
bin/annotator/tests/%.class: %.java
	mkdir -p bin
	javac -g -cp bin:../bin -d bin -sourcepath . $*.java

# Actually runs the annotator to create the annotated java file.
.PRECIOUS: %.output
%.output: %.jaif %.java bin/annotator/tests/%.class ../lib/plume.jar ../bin ../annotation-file-utilities.jar
	java \
	-cp bin:../annotation-file-utilities.jar \
	annotator.Main \
	${DEBUG} \
	--abbreviate=false \
	-d $*-output \
	$*.jaif \
	$*.java \
	2>&1 | tee $*.log
	mv $*-output/$*.java $*.output
	rm -rf $*-output

# Compare the output of the annotator and the goal file.
%.diff: %.goal %.output
	-diff -u $*.goal $*.output >& $*.diff

# Remove all .diff, .log files from the tests directory.
.PHONY: clean
clean :
	rm -rf bin
	rm -f *.diff
	rm -f *.log
	rm -f *.output
