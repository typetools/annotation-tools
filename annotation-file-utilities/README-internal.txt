This is the internal README for creating a distribution of the annotation
file utilities.

The distributed documentation file is annotation-file-utilities.html.

The annotator is the insert-annnotations-to-source tool of the annotation
file utilities.

The relevant ant build commands are:

  ant export            create and post to the web the entire distribution
  ant -projecthelp      list all other ant targets

Before you make a new release, you must manually update the version number
and date in the following places:
  In the manual:
    annotation-file-utilities.html (twice, including in the changelog)
  In the actual tools:
    src/annotator/Main.java
    annotations/scene-lib/src/annotations/io/classfile/ClassFileReader.java
    annotations/scene-lib/src/annotations/io/classfile/ClassFileWriter.java

