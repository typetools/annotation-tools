Annotation File Utilities README file

For user documentation, see file annotation-file-utilities.html .

===========================================================================

Contents

The contents of this directory are:

annotation-file-utilities.html
  Annotation File Utilities documentation.
  Most users should only have to read this file.
annotation-file-format.{html,pdf}
  Describes the annotation file format.
scripts/
  Contains Unix and Windows programs for transferring annotations among
  Java, class, and annotation files.
annotation-file-utilities.jar
  Java library used by the programs.
build.xml, src/, lib/, tests/, 
  For developers only:  buildfile, source code, libraries, tests.

===========================================================================

Making a release

To make a release (a distribution):

Be sure to run these instructions in an account that uses JDK 6, not JDK 7.

Manually update the version number and date in the following places:
    annotation-file-utilities.html  (in "Installation" section)
    changelog.html
    annotations/scene-lib/src/annotations/io/classfile/ClassFileReader.java

Create and post to the web the entire distribution:
  ant -e web

Run
  checklink -q -r http://types.cs.washington.edu/annotation-file-utilities/
and if there are any problems, re-make the distribution.

Tag the release, for example:
  hg tag 3.1

Send email to: checker-framework-discuss@googlegroups.com

===========================================================================
