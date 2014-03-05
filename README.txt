Annotation Tools README file
----------------------------

This directory contains the Annotation Tools.
When distributed, this is known as the Annotation File Utilities, which is
one of its components; see the annotation-file-utilities subdirectory.

The Annotation File Utilities homepage is:
  http://types.cs.washington.edu/annotation-file-utilities/
and it also appears in this directory as:
  annotation-file-utilities/annotation-file-utilities.html


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

asmx was branched off of asm 2.2.2, available at either of these locations:
  http://forge.objectweb.org/project/download.php?group_id=23&file_id=5769
  http://download.forge.objectweb.org/asm/asm-2.2.2.tar.gz
To see the changes, diff the current source files against the 2.2.2
files.

The diffs are complicated by the fact that in a few cases, a post-2.2.2
version of a file was added to asmx.
One example is file src/org/objectweb/asm/optimizer/shrink.properties.
