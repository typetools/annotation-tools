package annotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plume.FileIOException;
import plume.Option;
import plume.Options;
import plume.Pair;
import plume.UtilMDE;
import annotator.Source.CompilerException;
import annotator.find.Criteria;
import annotator.find.Insertion;
import annotator.find.Insertions;
import annotator.find.NewInsertion;
import annotator.find.ReceiverInsertion;
import annotator.find.TreeFinder;
import annotator.specification.IndexFileSpecification;
import annotator.specification.Specification;

import com.google.common.collect.SetMultimap;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.main.CommandLine;
import com.sun.tools.javac.tree.JCTree;

/**
 * This is the main class for the annotator, which inserts annotations in
 * Java source code.  You can call it as <tt>java annotator.Main</tt> or by
 * using the shell script <tt>insert-annotations-to-source</tt>.
 * <p>
 *
 * It takes as input
 * <ul>
 *   <li>annotation (index) files, which indicate the annotations to insert</li>
 *   <li>Java source files, into which the annotator inserts annotations</li>
 * </ul>
 * Use the --help option for full usage details.
 * <p>
 *
 * Annotations that are not for the specified Java files are ignored.
 */
public class Main {

  /** Directory in which output files are written. */
  @Option("-d <directory> Directory in which output files are written")
  public static String outdir = "annotated/";

  /**
   * If true, overwrite original source files (making a backup first).
   * Furthermore, if the backup files already exist, they are used instead
   * of the .java files.  This behavior permits a user to tweak the .jaif
   * file and re-run the annotator.
   * <p>
   *
   * Note that if the user runs the annotator with --in-place, makes edits,
   * and then re-runs the annotator with this --in-place option, those
   * edits are lost.  Similarly, if the user runs the annotator twice in a
   * row with --in-place, only the last set of annotations will appear in
   * the codebase at the end.
   * <p>
   *
   * To preserve changes when using the --in-place option, first remove the
   * backup files.  Or, use the <tt>-d .</tt> option, which makes (and
   * reads) no backup, instead of --in-place.
   */
  @Option("-i Overwrite original source files")
  public static boolean in_place = false;

  @Option("-a Abbreviate annotation names")
  public static boolean abbreviate = true;

  @Option("-c Insert annotations in comments")
  public static boolean comments = false;

  @Option("-o Omit given annotation")
  public static String omit_annotation;

  @Option("-h Print usage information and exit")
  public static boolean help = false;

  @Option("-v Verbose (print progress information)")
  public static boolean verbose;

  @Option("Debug (print debug information)")
  public static boolean debug = false;

  @Option("Print error stack")
  public static boolean print_error_stack = false;

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
  public static void main(String[] args) throws IOException {

    if (verbose) {
      System.out.printf("insert-annotations-to-source (%s)",
                        annotations.io.classfile.ClassFileReader.INDEX_UTILS_VERSION);
    }

    Options options = new Options(
        "Main [options] { ann-file | java-file | @arg-file } ...\n"
            + "(Contents of argfiles are expanded into the argument list.)",
        Main.class);
    String[] file_args;
    try {
      String[] cl_args = CommandLine.parse(args);
      file_args = options.parse_or_usage(cl_args);
    } catch (IOException ex) {
      System.err.println(ex);
      System.err.println("(For non-argfile beginning with \"@\", use \"@@\" for initial \"@\".");
      System.err.println("Alternative for filenames: indicate directory, e.g. as './@file'.");
      System.err.println("Alternative for flags: use '=', as in '-o=@Deprecated'.)");
      file_args = null;  // Eclipse compiler issue workaround
      System.exit(1);
    }

    if (debug) {
      TreeFinder.debug = true;
      Criteria.debug = true;
    }

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
    Insertions insertions = new Insertions();
    // The Java files into which to insert.
    List<String> javafiles = new ArrayList<String>();

    for (String arg : file_args) {
      if (arg.endsWith(".java")) {
        javafiles.add(arg);
      } else if (arg.endsWith(".jaif") ||
                 arg.endsWith(".jann")) {
        try {
          Specification spec = new IndexFileSpecification(arg);
          List<Insertion> parsedSpec = spec.parse();
          if (verbose || debug) {
            System.out.printf("Read %d annotations from %s%n",
                              parsedSpec.size(), arg);
          }
          if (omit_annotation != null) {
            List<Insertion> filtered = new ArrayList<Insertion>(parsedSpec.size());
            for (Insertion insertion : parsedSpec) {
              // TODO: this won't omit annotations if the insertion is more than
              // just the annotation (such as if the insertion is a cast
              // insertion or a 'this' parameter in a method declaration).
              if (! omit_annotation.equals(insertion.getText())) {
                filtered.add(insertion);
              }
            }
            parsedSpec = filtered;
            if (verbose || debug) {
              System.out.printf("After filtering: %d annotations from %s%n",
                                parsedSpec.size(), arg);
            }
          }
          insertions.addAll(parsedSpec);
        } catch (RuntimeException e) {
          if (e.getCause() != null
              && e.getCause() instanceof FileNotFoundException) {
            System.err.println("File not found: " + arg);
            System.exit(1);
          } else {
            throw e;
          }
        } catch (FileIOException e) {
          // Add 1 to the line number since line numbers in text editors are usually one-based.
          System.err.println("Error while parsing annotation file " + arg + " at line "
              + (e.lineNumber + 1) + ":");
          if (e.getMessage() != null) {
            System.err.println('\t' + e.getMessage());
          }
          if (e.getCause() != null && e.getCause().getMessage() != null) {
            System.err.println('\t' + e.getCause().getMessage());
          }
          if (debug) {
            e.printStackTrace();
          }
          System.exit(1);
        }
      } else {
        throw new Error("Unrecognized file extension: " + arg);
      }
    }

    if (debug) {
      System.out.printf("%d insertions, %d .java files%n", insertions.size(), javafiles.size());
    }
    if (debug) {
      System.out.printf("Insertions:%n");
      for (Insertion insertion : insertions) {
        System.out.printf("  %s%n", insertion);
      }
    }

    for (String javafilename : javafiles) {

      if (verbose) {
        System.out.println("Processing " + javafilename);
      }

      File javafile = new File(javafilename);
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
      }

      Set<String> imports = new LinkedHashSet<String>();

      String fileSep = System.getProperty("file.separator");
      String fileLineSep = System.getProperty("line.separator");
      Source src;
      // Get the source file, and use it to obtain parse trees.
      try {
        // fileLineSep is set here so that exceptions can be caught
        fileLineSep = UtilMDE.inferLineSeparator(javafilename);
        src = new Source(javafilename);
        if (verbose) {
          System.out.printf("Parsed %s%n", javafilename);
        }
      } catch (CompilerException e) {
        e.printStackTrace();
        return;
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      int num_insertions = 0;
      String pkg = "";

      for (CompilationUnitTree cut : src.parse()) {
        JCTree.JCCompilationUnit tree = (JCTree.JCCompilationUnit) cut;
        ExpressionTree pkgExp = cut.getPackageName();
        pkg = pkgExp == null ? "" : pkgExp.toString();

        // Create a finder, and use it to get positions.
        TreeFinder finder = new TreeFinder(tree);
        if (debug) {
          TreeFinder.debug = true;
        }
        SetMultimap<Integer, Insertion> positions = finder.getPositions(tree, insertions);

        // Apply the positions to the source file.
        if (debug || verbose) {
          System.err.printf("getPositions returned %d positions in tree for %s%n", positions.size(), javafilename);
        }

        Set<Integer> positionKeysUnsorted = positions.keySet();
        Set<Integer> positionKeysSorted =
          new TreeSet<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
              return o1.compareTo(o2) * -1;
            }
          });
        positionKeysSorted.addAll(positionKeysUnsorted);
        for (Integer pos : positionKeysSorted) {
          boolean receiverInserted = false;
          boolean newInserted = false;
          List<Insertion> toInsertList = new ArrayList<Insertion>(positions.get(pos));
          Collections.reverse(toInsertList);
          if (debug) {
            System.out.printf("insertion pos: %d%n", pos);
          }
          assert pos >= 0
            : "pos is negative: " + pos + " " + toInsertList.get(0) + " " + javafilename;
          for (Insertion iToInsert : toInsertList) {
            // Possibly add whitespace after the insertion
            String trailingWhitespace = "";
            boolean gotSeparateLine = false;
            if (iToInsert.getSeparateLine()) {
              // System.out.printf("getSeparateLine=true for insertion at pos %d: %s%n", pos, iToInsert);
              int indentation = 0;
              while ((pos - indentation != 0)
                     // horizontal whitespace
                     && (src.charAt(pos-indentation-1) == ' '
                         || src.charAt(pos-indentation-1) == '\t')) {
                // System.out.printf("src.charAt(pos-indentation-1 == %d-%d-1)='%s'%n",
                //                   pos, indentation, src.charAt(pos-indentation-1));
                indentation++;
              }
              if ((pos - indentation == 0)
                  // horizontal whitespace
                  || (src.charAt(pos-indentation-1) == '\f'
                      || src.charAt(pos-indentation-1) == '\n'
                      || src.charAt(pos-indentation-1) == '\r')) {
                trailingWhitespace = fileLineSep + src.substring(pos-indentation, pos);
                gotSeparateLine = true;
              }
            }

            char precedingChar;
            if (pos != 0) {
              precedingChar = src.charAt(pos - 1);
            } else {
              precedingChar = '\0';
            }

            if (iToInsert.getKind() == Insertion.Kind.RECEIVER) {
              ReceiverInsertion ri = (ReceiverInsertion) iToInsert;
              ri.setAnnotationsOnly(receiverInserted);
              receiverInserted = true;
            } else if (iToInsert.getKind() == Insertion.Kind.NEW) {
              NewInsertion ni = (NewInsertion) iToInsert;
              ni.setAnnotationsOnly(newInserted);
              newInserted = true;
            }

            String toInsert = iToInsert.getText(comments, abbreviate,
                    gotSeparateLine, pos, precedingChar) + trailingWhitespace;
            if (abbreviate) {
              Set<String> packageNames = iToInsert.getPackageNames();
              if (debug) {
                System.out.printf("Need import %s%n  due to insertion %s%n",
                                  packageNames, toInsert);
              }
              imports.addAll(packageNames);
            }

            // If it's already there, don't re-insert.  This is a hack!
            // Also, I think this is already checked when constructing the
            // insertions.
            int precedingTextPos = pos-toInsert.length()-1;
            if (precedingTextPos >= 0) {
              String precedingTextPlusChar
                = src.getString().substring(precedingTextPos, pos);
              // System.out.println("Inserting " + toInsert + " at " + pos + " in code of length " + src.getString().length() + " with preceding text '" + precedingTextPlusChar + "'");
              if (toInsert.equals(precedingTextPlusChar.substring(0, toInsert.length()))
                  || toInsert.equals(precedingTextPlusChar.substring(1))) {
                if (debug) {
                    System.out.println("Inserting " + toInsert + " at " + pos + " in code of length " + src.getString().length() + " with preceding text '" + precedingTextPlusChar + "'");
                    System.out.println("Already present, skipping");
                }
                continue;
              }
            }

            // TODO: Neither the above hack nor this check should be
            // necessary.  Find out why re-insertions still occur and
            // fix properly.
            if (iToInsert.getInserted()) { continue; }
            src.insert(pos, toInsert);
            if (verbose) {
              System.out.print(".");
              num_insertions++;
              if ((num_insertions % 50) == 0) {
                System.out.println();   // terminate the line that contains dots
              }
            }
            if (debug) {
              System.out.println("Post-insertion source: " + src.getString());
            }
          }
        }
      }
      if (verbose) {
        if ((num_insertions % 50) != 0) {
          System.out.println();   // terminate the line that contains dots
        }
      }

      // insert import statements
      {
        if (debug) {
          System.out.println(imports.size() + " imports to insert");
          for (String classname : imports) {
            System.out.println("  " + classname);
          }
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
        for (String classname : imports) {
          String toInsert = "import " + classname + ";" + fileLineSep;
          src.insert(importIndex, toInsert);
          importIndex += toInsert.length();
        }
      }

      // Write the source file.
      File outfile = null;
      try {
        if (in_place) {
          outfile = javafile;
          if (verbose) {
            System.out.printf("Renaming %s to %s%n", javafile, unannotated);
          }
          boolean success = javafile.renameTo(unannotated);
          if (! success) {
            throw new Error(String.format("Failed renaming %s to %s",
                                          javafile, unannotated));
          }
        } else {
          if (pkg.isEmpty()) {
            outfile = new File(outdir, javafile.getName());
          } else {
            String[] pkgPath = pkg.split("\\.");
            StringBuilder sb = new StringBuilder(outdir);
            for (int i = 0 ; i < pkgPath.length ; i++) {
              sb.append(fileSep).append(pkgPath[i]);
            }
            outfile = new File(sb.toString(), javafile.getName());
          }
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

  /**
   * Separates the annotation class from its arguments.
   *
   * @return given <code>@foo(bar)</code> it returns the pair <code>{ @foo, (bar) }</code>.
   */
  public static Pair<String,String> removeArgs(String s) {
    int pidx = s.indexOf("(");
    return (pidx == -1) ?
        Pair.of(s, (String)null) :
        Pair.of(s.substring(0, pidx), s.substring(pidx));
  }

}
