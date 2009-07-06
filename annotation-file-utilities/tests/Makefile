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

default : all

.PHONY: all
all : clean compile $(DIFFS) results

# Display results of all .diff files.
.PHONY: results
results: VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@java VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
VerifyDiffs.class : VerifyDiffs.java
	@javac VerifyDiffs.java

# Compiles the test cases as needed (by verbose about this).
compile :
	javac -g -d . *.java

# Actually runs the annotator to create the annotated java file.
.PRECIOUS: %.output
%.output:
	java -cp ../lib/utilMDE.jar:.:../bin:${CLASSPATH} \
	annotator.Main \
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
	rm -rf *.class
	rm -f *.diff
	rm -f *.log
	rm -f *.output
