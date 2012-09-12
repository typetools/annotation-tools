package annotations.io.classfile;

import checkers.nullness.quals.*;

import java.io.*;
import plume.*;

import org.objectweb.asm.ClassReader;

import annotations.Annotation;
import annotations.AnnotationFactory;
import annotations.el.AScene;
import annotations.io.IndexFileParser;

/**
 * A <code> ClassFileWriter </code> provides methods for inserting annotations
 *  from an {@link annotations.el.AScene} into a class file.
 */
public class ClassFileWriter {

  @Option("-h print usage information and exit")
  public static boolean help = false;

  @Option("print version information and exit")
  public static boolean version = false;

  private static String linesep = System.getProperty("line.separator");

  static String usage
    = "usage: insert-annotations [options] class1 indexfile1 class2 indexfile2 ..."
    + ""
    + linesep
    + "For each class/index file pair (a.b.C a.b.C.jaif), read annotations from"
    + linesep
    + "the index file a.b.C.jaif and insert them into the class a.b.C, then"
    + linesep
    + "output the merged class file to a.b.C.class"
    + linesep
    + "Each class is either a fully-qualified name of a class on your classpath,"
    + linesep
    + "or a path to a .class file, such as e.g. /.../path/to/a/b/C.class ."
    + linesep
    + "Options:";

  /**
   * Main method meant to a a convenient way to write annotations from an index
   * file to a class file.  For programmatic access to this
   * tool, one should probably use the insert() methods instead.
   *
   * Usage: java annotations.io.ClassFileWriter options [classfile indexfile] ...
   *
   * <options> include:
   *   -h, --help   print usage information and exit
   *   --version    print version information and exit
   *
   * @param args options and classes and index files to analyze;
   * @throws IOException if a class file or index file cannot be opened/written
   */
  public static void main(String[] args) throws IOException {
    boolean printUsage = false;
    Options options = new Options(usage, ClassFileWriter.class);

    args = options.parse_or_usage(args);
    if (version) {
      System.out.printf("insert-annotations (%s)",
                        ClassFileReader.INDEX_UTILS_VERSION);
    }
    if (help) {
      options.print_usage();
    }
    if (version || help) {
      System.exit(-1);
    }

    if (args.length == 0) {
      options.print_usage("No arguments given.");
      System.exit(-1);
    }
    if (args.length % 2 == 1) {
      options.print_usage("Must supply an even number of arguments.");
      System.exit(-1);
    }

    // check args for well-formed names
    for (int i = 0; i < args.length; i += 2) {
      if (!ClassFileReader.checkClass(args[i])) {
        System.exit(-1);
      }
    }

    for (int i = 0; i < args.length; i++) {

     String className = args[i];
     i++;
     if (i >= args.length) {
       System.out.println("Error: incorrect number of arguments");
       System.out.println("Run insert-annotations --help for usage information");
       return;
     }
     String indexFileName = args[i];

     AScene scene = new AScene();

     IndexFileParser.parseFile(indexFileName, scene);

     // annotations loaded from index file into scene, now insert them
     // into class file
     try {
       if (className.endsWith(".class")) {
         System.out.printf("Adding annotations to class file %s%n", className);
         insert(scene, className, true);
       } else {
         String outputFileName = className + ".class";
         System.out.printf("Reading class file %s; writing with annotations to %s%n",
                           className, outputFileName);
         insert(scene, className, outputFileName, true);
       }
     } catch(IOException e) {
       System.out.printf("IOException: %s%n", e.getMessage());
       return;
     } catch(Exception e) {
       System.out.println("Unknown error trying to insert annotations from: " +
           indexFileName + " to " + className);
       e.printStackTrace();
       System.out.println("Please submit a bug report at");
       System.out.println("  http://code.google.com/p/annotation-tools/issues");
       System.out.println("Be sure to include a copy of the following output trace, instructions on how");
       System.out.println("to reproduce this error, and all input files.  Thanks!");
       return;
     }
    }

  }

  /**
   * Inserts the annotations contained in <code> scene </code> into
   * the class file contained in <code> fileName </code>, and write
   * the result back into <code> fileName </code>.
   *
   * @param scene the scene containing the annotations to insert into a class
   * @param fileName the file name of the class the annotations should be
   * inserted into.  Should be a file name that can be resolved from
   * the current working directory, which means it should end in ".class"
   * for standard Java class files.
   * @param overwrite controls behavior when an annotation exists on a
   * particular element in both the scene and the class file.  If true,
   * then the one from the scene is used; else the the existing annotation
   * in the class file is retained.
   * @throws IOException if there is a problem reading from or writing to
   * <code> fileName </code>
   */
  public static void insert(
      AScene scene, String fileName, boolean overwrite)
  throws IOException {
    assert fileName.endsWith(".class");

    // can't just call other insert, because this closes the input stream
    InputStream in = new FileInputStream(fileName);
    ClassReader cr = new ClassReader(in);
    in.close();

    ClassAnnotationSceneWriter cw =
      new ClassAnnotationSceneWriter(scene, overwrite);
    cr.accept(cw, false);

    OutputStream fos = new FileOutputStream(fileName);
    fos.write(cw.toByteArray());
    fos.close();
  }

  /**
   * Inserts the annotations contained in <code> scene </code> into
   * the class file read from <code> in </code>, and writes the resulting
   * class file into <code> out </code>.  <code> in </code> should be a stream
   * of bytes that specify a valid Java class file, and <code> out </code> will
   * contain a stream of bytes in the same format, and will also contain the
   * annotations from <code> scene </code>.
   *
   * @param scene the scene containing the annotations to insert into a class
   * @param in the input stream from which to read a class
   * @param out the output stream the merged class should be written to
   * @param overwrite controls behavior when an annotation exists on a
   * particular element in both the scene and the class file.  If true,
   * then the one from the scene is used; else the the existing annotation
   * in the class file is retained.
   * @throws IOException if there is a problem reading from <code> in </code> or
   * writing to <code> out </code>
   */
  public static void insert(AScene scene, InputStream in,
      OutputStream out, boolean overwrite) throws IOException {
    ClassReader cr = new ClassReader(in);

    ClassAnnotationSceneWriter cw =
      new ClassAnnotationSceneWriter(scene, overwrite);

    cr.accept(cw, false);

    out.write(cw.toByteArray());
  }

  /**
   * Inserts the annotations contained in <code> scene </code> into
   * the class <code> in </code>, and writes the resulting
   * class file into <code> out </code>.  <code> in </code> should be the
   * name of a fully-qualified class, and <code> out </code> should be the
   * name of a file to output the resulting class file to.
   *
   * @param scene the scene containing the annotations to insert into a class
   * @param className the fully qualified class to read
   * @param outputFileName the name of the output file the class should be written to
   * @param overwrite controls behavior when an annotation exists on a
   * particular element in both the scene and the class file.  If true,
   * then the one from the scene is used; else the the existing annotation
   * in the class file is retained.
   * @throws IOException if there is a problem reading from <code> in </code> or
   * writing to <code> out </code>
   */
  public static void insert(AScene scene,
      String className, String outputFileName, boolean overwrite) throws IOException {
    ClassReader cr = new ClassReader(className);

    ClassAnnotationSceneWriter cw =
      new ClassAnnotationSceneWriter(scene, overwrite);

    cr.accept(cw, false);

    OutputStream fos = new FileOutputStream(outputFileName);
    fos.write(cw.toByteArray());
    fos.close();
  }
}
