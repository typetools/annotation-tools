package annotator;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import utilMDE.*;

import annotator.find.Insertion;
import annotator.find.TreeFinder;
import annotator.Source;
import annotator.Source.CompilerException;
import annotator.specification.IndexFileSpecification;
import annotator.specification.Specification;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * This is the main class for the annotator, which inserts annotations in
 * Java source code.  It takes as input
 * <ul>
 *   <li>annotation (index) files, which indcate the annotations to insert</li>
 *   <li>Java source files, into which the annotator inserts annotations</li>
 * </ul>
 * Use the --help option for full usage details.
 * <p>
 *
 * Annotations that are not for the specified Java files are ignored.
 */
public class Main {

  public static final String INDEX_UTILS_VERSION =
    "Annotation file utilities: insert-annotations-to-source v2.3";

  /** Directory in which output files are written. */
  @Option("-d <directory> Directory in which output files are written")
  public static String outdir = "annotated/";

  // It's already possible to emulate this via
  //   -d .
  // but the --in-place argument is more convenient and explicit, and it
  // makes a backup so it can be run multiple times without restoring the
  // original state of the .java files.
  /** Directory in which output files are written. */
  @Option("-i Overwrite original source files")
  public static boolean in_place = false;

  @Option("-h Print usage information and exit")
  public static boolean help = false;

  @Option("-a Abbreviate annotation names")
  public static boolean abbreviate = true;

  @Option("-c Insert annotations in comments")
  public static boolean comments = false;

  @Option("-v Verbose (print progress information)")
  public static boolean verbose;

  @Option("Debug (print debug information)")
  public static boolean debug = false;

  // Implementation details:
  //  1. The annotator partially compiles source
  //     files using the compiler API (JSR-199), obtaining an AST.
  //  2. The annotator reads the specification file, producing a set of
  //     annotator.find.Insertions.  Insertions completely specify what to
  //     write (as a String, which is ultimately translated according to the
  //     keyword file) and how to write it (as annotator.find.Criteria).
  //  3. It then traverses the tree, looking for nodes that satisfy the
  //     Insertion Criteria, translating the Insertion text against the
  //     keyword file, and inserting the annotations into the source file.

  /**
   * Runs the annotator, parsing the source and spec files and applying
   * the annotations.
   */
  public static void main(String[] args) {

    if (verbose) {
      System.out.println(INDEX_UTILS_VERSION);
    }

    Options options = new Options("Main [options] ann-file... java-file...", Main.class);
    String[] file_args = options.parse_and_usage (args);

    if (help) {
      options.print_usage();
      System.exit(0);
    }

    if (in_place && outdir != "annotated/") { // interned
      options.print_usage("The --outdir and --in-place options are mutually exclusive.");
      System.exit(1);
    }

    if (file_args.length < 2) {
      options.print_usage("Supplied %d arguments, at least 2 needed%n", file_args.length);
      System.exit(1);
    }

    // The insertions specified by the annotation files.
    List<Insertion> insertions = new ArrayList<Insertion>();
    // The Java files into which to insert.
    List<String> javafiles = new ArrayList<String>();

    for (String arg : file_args) {
      if (arg.endsWith(".java")) {
        javafiles.add(arg);
      } else if (arg.endsWith(".jaif")) {
        try {
          Specification spec = new IndexFileSpecification(arg);
          List<Insertion> parsedSpec = spec.parse();
          insertions.addAll(parsedSpec);
          if (verbose || debug) {
            System.out.printf("Read %d annotations from %s%n",
                              parsedSpec.size(), arg);
          }
        } catch (FileIOException e) {
          System.err.println("Error while parsing annotation file " + arg);
          if (e.getMessage() != null) {
            System.err.println(e.getMessage());
          }
          e.printStackTrace();
          System.exit(1);
        }
      } else {
        throw new Error("Unrecognized file extension: " + arg);
      }
    }

    if (debug) {
      System.err.printf("%d insertions, %d .java files%n", insertions.size(), javafiles.size());
    }
    if (debug) {
      System.err.printf("Insertions:%n");
      for (Insertion insertion : insertions) {
        System.err.printf("  %s%n", insertion);
      }
    }

    for (String javafilename : javafiles) {

      if (verbose) {
        System.out.println("Processing " + javafilename);
      }

      File javafile = new File(javafilename);

      File outfile;
      File unannotated = new File(javafilename + ".unannotated");
      if (in_place) {
        // It doesn't make sense to check timestamps;
        // if the .java.unannotated file exists, then just use it.
        // A user can rename that file back to just .java to cause the
        // .java file to be read.
        if (unannotated.exists()) {
          if (verbose) {
            System.out.printf("Renaming %s to %s%n", unannotated, javafile);
          }
          boolean success = unannotated.renameTo(javafile);
          if (! success) {
            throw new Error(String.format("Failed renaming %s to %s",
                                          unannotated, javafile));
          }
        }
        outfile = javafile;
      } else {
        String baseName;
        if (javafile.isAbsolute()) {
          baseName = javafile.getName();
        } else {
          baseName = javafile.getPath();
        }
        outfile = new File(outdir, baseName);
      }

      Set<String> imports = new LinkedHashSet<String>();

      String fileLineSep = System.getProperty("line.separator");
      Source src;
      // Get the source file, and use it to obtain parse trees.
      try {
        // fileLineSep is set here so that exceptions can be caught
        fileLineSep = UtilMDE.inferLineSeparator(javafilename);
        src = new Source(javafilename);
      } catch (CompilerException e) {
        e.printStackTrace();
        return;
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      for (CompilationUnitTree tree : src.parse()) {

        // Create a finder, and use it to get positions.
        TreeFinder finder = new TreeFinder(tree);
        Map<Integer, String> positions = finder.getPositions(tree, insertions);

        // Apply the positions to the source file.
        if (debug) {
          System.err.printf("%d positions in tree for %s%n", positions.size(), javafilename);
        }

        for (Integer pos : positions.keySet()) {
          String toInsert = positions.get(pos).trim();
          if (! toInsert.startsWith("@")) {
            throw new Error("Insertion doesn't start with '@': " + toInsert);
          }
          if (abbreviate) {
            int nameEnd = toInsert.indexOf("(");
            if (nameEnd == -1) {
              nameEnd = toInsert.length();
            }
            int dotIndex = toInsert.lastIndexOf(".", nameEnd);
            if (dotIndex != -1) {
              imports.add(toInsert.substring(1, nameEnd));
              toInsert = "@" + toInsert.substring(dotIndex + 1);
            }
          }
          if (comments) {
            toInsert = "/*" + toInsert + "*/";
          }

          // Possibly add a leading space before the insertion
          if (pos != 0) {
            char precedingChar = src.charAt(pos-1);
            if (! (Character.isWhitespace(precedingChar)
                   // No space if it's the first formal or generic parameter
                   || precedingChar == '('
                   || precedingChar == '<')) {
              toInsert = " " + toInsert;
            }
          }
          // If it's already there, don't re-insert.  This is a hack!
          int precedingTextPos = pos-toInsert.length()-1;
          if (precedingTextPos >= 0) {
            String precedingTextPlusChar
              = src.getString().substring(precedingTextPos, pos);
            // System.out.println("Inserting " + toInsert + " at " + pos + " in code of length " + src.getString().length() + " with preceding text '" + precedingTextPlusChar + "'");
            if (toInsert.equals(precedingTextPlusChar.substring(0, toInsert.length()))
                || toInsert.equals(precedingTextPlusChar.substring(1))) {
              if (debug) {
                System.out.println("Already present, skipping");
              }
              continue;
            }
          }
          // add trailing whitespace
          if (pos==0) {
            toInsert = toInsert + fileLineSep;
          } else {
            toInsert = toInsert + " ";
          }
          src.insert(pos, toInsert);
          if (debug) {
            System.out.println("Post-insertion source: " + src.getString());
          }
        }
      }

      // insert import statements
      {
        if (debug) {
          System.out.println(imports.size() + " imports to insert");
        }
        Pattern importPattern = Pattern.compile("(?m)^import\\b");
        Pattern packagePattern = Pattern.compile("(?m)^package\\b.*;(\\n|\\r\\n?)");
        int importIndex = 0;      // default: beginning of file
        String srcString = src.getString();
        Matcher m;
        m = importPattern.matcher(srcString);
        if (m.find()) {
          importIndex = m.start();
        } else {
          // if (debug) {
          //   System.out.println("Didn't find import in " + srcString);
          // }
          m = packagePattern.matcher(srcString);
          if (m.find()) {
            importIndex = m.end();
          }
        }
        String lineSep = System.getProperty("line.separator");
        for (String classname : imports) {
          String toInsert = "import " + classname + ";" + fileLineSep;
          src.insert(importIndex, toInsert);
          importIndex += toInsert.length();
        }
      }

      // Write the source file.
      try {
        if (in_place) {
          if (verbose) {
            System.out.printf("Renaming %s to %s%n", javafile, unannotated);
          }
          boolean success = javafile.renameTo(unannotated);
          if (! success) {
            throw new Error(String.format("Failed renaming %s to %s",
                                          javafile, unannotated));
          }
        } else {
          outfile.getParentFile().mkdirs();
        }
        OutputStream output = new FileOutputStream(outfile);
        if (verbose) {
          System.out.printf("Writing %s%n", outfile);
        }
        src.write(output);
        output.close();
      } catch (IOException e) {
        System.err.println("Problem while writing file " + outfile);
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  ///
  /// Utility methods
  ///

  public static String pathToString(TreePath path) {
    if (path == null)
      return "null";
    return treeToString(path.getLeaf());
  }

  public static String treeToString(Tree node) {
    String asString = node.toString();
    String oneLine = firstLine(asString);
    return "\"" + oneLine + "\"";
  }

  /**
   * Return the first non-empty line of the string, adding an ellipsis
   * (...) if the string was truncated.
   */
  public static String firstLine(String s) {
    while (s.startsWith("\n")) {
      s = s.substring(1);
    }
    int newlineIndex = s.indexOf('\n');
    if (newlineIndex == -1) {
      return s;
    } else {
      return s.substring(0, newlineIndex) + "...";
    }
  }

}
