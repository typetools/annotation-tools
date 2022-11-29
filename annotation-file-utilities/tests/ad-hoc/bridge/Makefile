.PHONY: all
all: bridge no-bridge

JAVAC?=javac

# In Java 8, include Java 9 javac.  Without this, tests fail with:
#   Exception in thread "main" java.lang.NoSuchFieldError: ANNOTATION_PROCESSOR_MODULE_PATH
ifeq ($(shell java -version 2>&1 | grep version | grep 1.8 > /dev/null; printf $$?), 0)
export JAVA:=$(JAVA) -Xbootclasspath/p:../../../lib/javac-9+181-r4173-1.jar
endif

JAVACVERSION = $(shell $(JAVAC) -version 2>&1)

.PHONY: bridge
# Counting bridge methods, there should be 3 @A annotations.
bridge: C.class
	CLASSPATH=.:${CLASSPATH} ../../../scripts/extract-annotations C.class
# skip test if using javac 1.7, which doesn't copy annotations to bridge methods
	test 3 -eq `grep -c -w '@A' C.jaif` || (echo "Didn't find 3 '@A' in C.jaif:" && cat C.jaif && false)

.PHONY: no-bridge
# Not counting bridge methods, there should be 2 @A annotations.
# -b ignores annotations on bridge methods
no-bridge: C.class
	CLASSPATH=.:${CLASSPATH} ../../../scripts/extract-annotations -b C.class
	test 2 -eq `grep -c -w '@A' C.jaif` || (echo "Didn't find 2 '@A' in C.jaif:" && cat C.jaif && false)

C.class: C.java
	$(JAVAC) -g $(JAVACTARGET) C.java

.PHONY: clean
clean:
	rm -rf *.class C.jaif annotated out

