all: out

out: C.jaif C.class
	rm -f out
	insert-annotations-to-source C.jaif C.java |& tee out
	test ! -s out

C.class: C.java
	javac -g C.java

.PHONY: clean
clean:
	rm -rf *.class annotated out

