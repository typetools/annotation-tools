package annotations.io.classfile;

import checkers.nullness.quals.*;

import java.io.*;
import plume.*;

import org.objectweb.asm.ClassReader;

import annotations.*;
import annotations.el.AScene;
import annotations.io.IndexFileWriter;

/**
 * A <code> ClassFileReader </code> provides methods for reading in annotations
 *  from a class file into an {@link annotations.el.AScene}.
 */
public class ClassFileReader {

  public static final String INDEX_UTILS_VERSION
    = "Annotation File Utilities v3.4";

  @Option("-h print usage information and exit")
  public static boolean help = false;
 
  @Option("print version information and exit")
  public static boolean version = false;
 
  private static String linesep = System.getProperty("line.separator");

  static String usage
    = "extract-annotations [options] class1 class2 ..."
    + linesep
    + "Each argument is a class a.b.C (that is on your classpath) or a class file"
    + linesep
    + "a/b/C.class.  Extracts the annotations from each such argument and prints"
    + linesep
    + "them in index-file format to a.b.C.jaif .  Options:";

  /**
   * From the command line, read annotations from a class file and write
   * them to an index file.  Also see the Anncat tool, which is more
   * versatile (and which calls this as a subroutine).
   * <p>
   *
   * For usage information, supply the -h or --help option.
   * <p>
   *
   * For programmatic access to this tool, use the read() methods instead.
   * <p>
   *
   * @param args options and classes to analyze;
   * @throws IOException if a class file cannot be found
   */
  public static void main(String[] args) throws IOException {
    Options options = new Options(usage, ClassFileReader.class);
    args = options.parse_or_usage(args);
    if (version) {
      System.out.printf("extract-annotations (%s)", INDEX_UTILS_VERSION);
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

    // check args for well-formed names
    for (String arg : args) {
      if (!checkClass(arg)) {
        System.exit(-1);
      }
    }

    for (String origName : args) {
      System.out.println("reading: " + origName);
      String className = origName;
      if (origName.endsWith(".class")) {
          origName = origName.replace(".class", "");
      }

      AScene scene = new AScene();
      try {
        if (className.endsWith(".class")) {
          read(scene, className);
        } else {
          readFromClass(scene, className);
        }
        String outputFile = origName + ".jaif";
        System.out.println("printing results to : " + outputFile);
        IndexFileWriter.write(scene, outputFile);
      } catch(IOException e) {
        System.out.println("There was an error in reading class: " + origName);
        System.out.println(
            "Did you ensure that this class is on your classpath?");
        return;
      } catch(Exception e) {
        System.out.println("Unknown error trying to extract annotations from: " +
            origName);
        System.out.println(e.getMessage());
        e.printStackTrace();
        System.out.println("Please submit a bug report at");
        System.out.println("  http://code.google.com/p/annotation-tools/issues");
        System.out.println("Be sure to include a copy of the output trace, instructions on how");
        System.out.println("to reproduce this error, and all input files.  Thanks!");
        return;
      }
    }
  }

  /**
   * If s is not a valid representation of a class, print a warning message
   * and return false.
   */
  public static boolean checkClass(String arg) {
    // check for invalid class file paths with '.'
    if (!arg.contains(".class") && arg.contains("/")) {
      System.out.println("Error: bad class " + arg);
      System.out.println("Use a fully qualified class name such as java.lang.Object");
      System.out.println("or a filename such as .../path/to/MyClass.class");
      return false;
    }
    return true;
  }

  /**
   * Reads the annotations from the class file <code> fileName </code>
   * and inserts them into <code> scene </code>.
   * <code> fileName </code> should be a file name that can be resolved from
   * the current working directory, which means it should end in ".class"
   * for standard Java class files.
   *
   * @param scene the scene into which the annotations should be inserted
   * @param fileName the file name of the class the annotations should be
   * read from
   * @throws IOException if there is a problem reading from
   * <code> fileName </code>
   */
  public static void read(AScene scene, String fileName)
  throws IOException {
    read(scene, new FileInputStream(fileName));
  }

  /**
   * Reads the annotations from the class <code> className </code>,
   * assumed to be in your classpath,
   * and inserts them into <code> scene </code>.
   *
   * @param scene the scene into which the annotations should be inserted
   * @param className the name of the class to read in
   * @throws IOException if there is a problem reading <code> className </code>
   */
  public static void readFromClass(AScene scene, String className)
  throws IOException {
    read(scene, new ClassReader(className));
  }

  /**
   * Reads the annotations from the class file <code> fileName </code>
   * and inserts them into <code> scene </code>.
   *
   * @param scene the scene into which the annotations should be inserted
   * @param in an input stream containing the class that the annotations
   * should be read from
   * @throws IOException if there is a problem reading from <code> in </code>
   */
  public static void read(AScene scene, InputStream in)
  throws IOException {
    read(scene, new ClassReader(in));
  }

  public static void read(AScene scene, ClassReader cr) {
    ClassAnnotationSceneReader ca = new ClassAnnotationSceneReader(scene);
    cr.accept(ca, true);
  }

}
