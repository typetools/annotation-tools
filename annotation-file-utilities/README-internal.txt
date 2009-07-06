This is the internal README for creating a distribution of the annotation
file utilities.  The distributed documentation file is
annotation-file-utilities.html.  The relevant commands are:

  ant export	create and post to the web the entire distribution
  ant help	list all possible options to create intermediate files

When you make a new release, you must manually update the version number
and date in the following places:

In the manual:
annotation-file-utilities.html

In the actual tools:
src/annotator/Main.java
annotations/scene-lib/src/annotations/io/classfile/ClassFileReader.java
annotations/scene-lib/src/annotations/io/classfile/ClassFileWriter.java

The annotator is the insert-annnotations-to-source tool of the annotation
file utilities.  The main documentation is at

src/annotator/Main.java
