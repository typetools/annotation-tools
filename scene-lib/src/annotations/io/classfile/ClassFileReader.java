package annotations.io.classfile;

import checkers.nullness.quals.*;

import java.io.*;

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
    = "Annotation File Utilities v3.1";

  static String usage
    = "usage: extract-annotations\n"
    + "            <options> \n"
    + "            <space-separated list of classes to analyze>\n"
    + "\n"
    + "       <options> include: \n"
    + "         -h, --help       print usage information and exit\n"
    + "         --version        print version information and exit\n"
    + "\n"
    + "For each class a.b.C (or class file a/b/C.class) given, extracts\n"
    + "the annotations from that class and prints them in index-file format\n"
    + "to a.b.C.jaif .\n"
    + "Note that, if using the qualified class name, the class a.b.C must be\n"
    + "located in your classpath.\n";


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
    boolean printUsage = false;
    for (String arg : args) {
      arg = arg.trim();
      if (arg.equals("-h") || arg.equals("-help") || arg.equals("--help")) {
        printUsage = true;
      }
    }

    for (String arg: args) {
      arg = arg.trim();
      if (arg.equals("-version") || arg.equals("--version")) {
        System.out.printf("extract-annotations (%s)",
                          INDEX_UTILS_VERSION);
        if (!printUsage) {
          return;
        }
      }
    }

    if (args.length == 0 || printUsage) {
      System.out.println(usage);
      return;
    }

    // check args for well-formed names
    for (String arg : args) {
      arg = arg.trim();
      // check for invalid class file paths with '.'
      if (!arg.contains(".class") && arg.contains("/")) {
        System.out.println("Error: " + arg +
            " does not appear to be a fully qualified class name or path");
        System.out.print(" please use names such as java.lang.Object");
        System.out.print(" instead of Ljava/lang/Object,");
        System.out.println(" or use class file paths as /.../path/to/MyClass.class");
        return;
      }
    }

    for (String origName : args) {
      if (origName.startsWith("-")) {
        continue; // ignore options
      }
      origName = origName.trim();
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
        System.out.println("Uknown error trying to extract annotations from: " +
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

  public static void read(AScene scene, ClassReader cr)
  {
      ClassAnnotationSceneReader ca = new ClassAnnotationSceneReader(scene);
      cr.accept(ca, true);
  }

}
