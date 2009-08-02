### This file is out of date and needs to be updated, particularly with
### respect to directories, checkouts, and environment variables.

Annotations Repository README file
----------------------------------

This README.txt file is for maintainers (not users), so it does not appear
in the distribution.


JSR 308 compiler
----------------

The JSR 308 compiler appears in a separate repository, langtools, that is a
copy of Sun's langtools repository.  It contains javac, javap, etc.  It is
distributed as the JSR 308 reference implementation and will eventually be
folded into the Sun JDK.

The annotations repository contains everything else (listed below) --
things that aren't & won't be part of Sun's JDK implementation.

Documentation for the compiler may be found at
  langtools/README-jsr308.html
which is also file "README-jsr308.html" in the compiler distribution.

The jsr308-langtools repository should be a sibling of checker-framework.
To check out a copy of the jsr308-langtools repository, use these commands:
  hg clone https://jsr308-langtools.googlecode.com/hg/ jsr308-langtools  


JSR 308 specification
---------------------

The JSR 308 specification is found with the JSR 308 compiler (the
"prototype" version), in directory doc/ .


Checker Framework
-----------------

Documentation for the checkers may be found in the manual in checkers/manual/.
To generate the manual (in PDF and HTML), see checkers/manual, and run
  make
The manual is also available in the checkers distribution (in checkers/manual/)
and from the JSR 308 web page.

Checker Framework API documentation can be generated from the checkers build
file. See checkers/ and run
  ant docs
to generate Javadoc to checkers/doc/.


Making a release
----------------

Release instructions and the release ant script -- for both the compiler
and the Checker Framework -- are located in the release/
directory. To make a release (either public, or on a private page for testing),
see release/README-maintainers.html.


Annotation file utilities
-------------------------

To create the distribution, see the annotation-file-utilities/ directory, and run
  ant


Adding a new target_type
------------------------

Whenever a new target_type is added to the specification in:
  annotations/doc/design.tex
(which is on the web as
http://groups.csail.mit.edu/pag/jsr308/specification/java-annotation-design.html#sec:class-file:ext:target_type)
the following needs to be done to fully incorporate it into the rest of the
system.

   - Update the annotation index file specification at
       annotations/doc/annotation-file-format.tex
     This will appear on the web the next time the Annotation File
     Utilities are distributed.
   - Incorporate it into the annotation scene library in the correct 
       manner (depending on the target_type) at annotations/scene-lib
   - Update the parser and writer as specified by the above two points
   - Update the asmx classfile parser to read the new attribute:
       annotations/asmx/src/org/objectweb/asm/ClassReader.java
   - Writing to classfile for extended annotations only requires modifying
       annotations/asmx/src/org/objectweb/asm/ExtendedAnnotationWriter.java
   - Modify the asmx extended annotation interface:
       annotations/asmx/src/org/objectweb/asm/ExtendedAnnotationVisitor.java
       Making this change will reveal all the remaining places where other
       changes might be necessary.
   - Update the annotator (.jaif -> .java) to understand the new type:
	    annotations/annotator


Checking out the Annotations and Javarifier projects
----------------------------------------------------

This is a guide to setting up the basic structure for the annotations
and Javarifier projects from a clean CVS/SVN checkout.

These projects require the use of JDK 1.6 (or later) in order to work.
These commands should report something along the lines of "1.6" or "1.7"
and not "1.5":

  java -version 
  javac -version

1. Checkout all the projects required to build and run the Javarifier:

ant checkout-all

Documentation appears at:
  jsr308-langtools/README-jsr308.html
  annotation-tools/annotation-file-utilities/annotation-file-utilities.html
  checker-framework/checkers/manual/
  javarifier/README


asmx
----

asmx contains modifications to asm to allow it to read and write JSR
308 annotations to/from class files.

The most modified classes are (in org.objectweb.asm):
 * ClassReader
 * ClassWriter
 * ExtendedAnnotationVisitor
 * ExtendedAnnotationWriter

Most of the changes are delimited by
//jaime
// end jaime

asmx was branched off of asm 2.2.2, available here:
  http://forge.objectweb.org/project/download.php?group_id=23&file_id=5769
To see the changes, diff the current source files against the 2.2.2
files.

The diffs are complicated by the fact that in a few cases, a post-2.2.2
version of a file was added to asmx.
One example is file src/org/objectweb/asm/optimizer/shrink.properties.
