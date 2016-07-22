.PHONY: all
all: bridge no-bridge

XJAVAC?=javac

.PHONY: bridge
# Counting bridge methods, there should be 3 @A annotations.
bridge: C.class
	../../../scripts/extract-annotations C.class
	test 3 -eq `grep -c -w '@A' C.jaif` || (echo "Didn't find 3 '@A' in C.jaif:" && cat C.jaif && false)

.PHONY: no-bridge
# Not counting bridge methods, there should be 2 @A annotations.
# -b ignores annotations on bridge methods
no-bridge: C.class
	../../../scripts/extract-annotations -b C.class
	test 2 -eq `grep -c -w '@A' C.jaif` || (echo "Didn't find 2 '@A' in C.jaif:" && cat C.jaif && false)

C.class: C.java
	$(XJAVAC) -g C.java

.PHONY: clean
clean:
	rm -rf *.class C.jaif annotated out

