.PHONY: all
all: bridge no-bridge

.PHONY: bridge
bridge: C.class
	../../../scripts/extract-annotations C.class
	test 3 -eq `grep -c -w '@A' C.jaif`

.PHONY: no-bridge
no-bridge: C.class
	../../../scripts/extract-annotations -b C.class
	test 2 -eq `grep -c -w '@A' C.jaif`

C.class: C.java
	$(JAVAC) -g C.java

.PHONY: clean
clean:
	rm -rf *.class C.jaif annotated out

