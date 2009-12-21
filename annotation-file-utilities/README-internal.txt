This is the internal README for creating a distribution of the Annotation
File Utilities.

The distributed documentation file is annotation-file-utilities.html.

The annotator is the insert-annnotations-to-source tool of the Annotation
File Utilities.

For a list of all Ant targets, run:  
  ant -projecthelp

===========================================================================

To make a release:

Manually update the version number and date in the following places:
    annotation-file-utilities.html
    changelog.html
    src/annotator/Main.java
    annotations/scene-lib/src/annotations/io/classfile/ClassFileReader.java
    annotations/scene-lib/src/annotations/io/classfile/ClassFileWriter.java

Run 
  ant web
create and post to the web the entire distribution.

Run
  checklink -q -r http://types.cs.washington.edu/annotation-file-utilities/
and if there are any problems, re-make the distribution.

Send email to: jsr308-discuss@googlegroups.com
