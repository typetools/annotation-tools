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
build.xml, src/, lib/, tests/
  For developers only:  buildfile, source code, libraries, tests.

===========================================================================

Notes

To build annotation-file-format.{html,pdf} your BIBINPUTS environment
variable must be set like so:

export BIBINPUTS=.:/path/to/plume/bib

plume-bib is available at https://github.com/mernst/plume-bib .

===========================================================================

Making a release

To make a release (a distribution):

Be sure to run these instructions in an account that uses JDK 7, not JDK 8.

Write a description of the most significant changes in:
    changelog.html
It may be helpful to examine the changes since the last release:
  git log v3.5.3..
  git diff v3.5.3..

Manually update the version number and date in the following places:
    annotation-file-utilities.html  (in "Installation" section)
    changelog.html
    annotations/scene-lib/src/annotations/io/classfile/ClassFileReader.java

Create and post to the web the entire distribution:
  ant -e web

Run
  checklink -q -r https://checkerframework.org/annotation-file-utilities/
and if there are any problems, re-make the distribution.

Tag the release, for example:
  git tag v3.5.3

Send email to: checker-framework-discuss@googlegroups.com

===========================================================================
