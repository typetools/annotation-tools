package annotator;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import plume.*;

import annotator.find.Insertion;
import annotator.find.TreeFinder;
import annotator.Source;
import annotator.Source.CompilerException;
import annotator.specification.IndexFileSpecification;
import annotator.specification.Specification;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

import com.google.common.collect.*;

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

  @Option("-h Print usage information and exit")
  public static boolean help = false;

  @Option("-a Abbreviate annotation names")
  public static boolean abbreviate = true;

  @Option("-c Insert annotations in comments")
  public static boolean comments = false;

  @Option("-o Omit given annotation")
  public static String omit_annotation;

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
      System.out.printf("insert-annotations-to-source (%s)",
                        annotations.io.classfile.ClassFileReader.INDEX_UTILS_VERSION);
    }

    Options options = new Options("Main [options] ann-file... java-file...", Main.class);
    String[] file_args = options.parse_or_usage (args);

    if (debug) {
      TreeFinder.debug = true;
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
    List<Insertion> insertions = new ArrayList<Insertion>();
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

      for (CompilationUnitTree tree : src.parse()) {

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
        Set<Integer> positionKeysSorted = new TreeSet<Integer>(new TreeFinder.ReverseIntegerComparator());
        positionKeysSorted.addAll(positionKeysUnsorted);
        for (Integer pos : positionKeysSorted) {
          List<Insertion> toInsertList = new ArrayList<Insertion>(positions.get(pos));
          Collections.reverse(toInsertList);
          if (debug) {
            System.out.printf("insertion pos: %d%n", pos);
          }
          assert pos >= 0
            : "pos is negative: " + pos + " " + toInsertList.get(0) + " " + javafilename;
          for (Insertion iToInsert : toInsertList) {
            String toInsert = iToInsert.getText();
            if (! (toInsert.startsWith("@")
                   || toInsert.startsWith("extends @"))) {
              throw new Error("Insertion doesn't start with '@': " + toInsert);
            }
            if (abbreviate) {
              Pair<String,String> ps = removePackage(toInsert);
              if (ps.a != null) {
                if (debug && !imports.contains(ps.a)) {
                  System.out.printf("Need import %s%n  due to insertion %s%n",
                                    ps.a, toInsert);
                }
                imports.add(ps.a);
              }
              toInsert = ps.b;
            }
            if (comments) {
              if (toInsert.startsWith("extends ")) {
                toInsert = "extends /*"
                  + toInsert.substring(8, toInsert.length()-7)
                  + "*/ Object";
              } else {
                toInsert = "/*" + toInsert + "*/";
              }
            }

            // Possibly add whitespace after the insertion
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
                toInsert = toInsert + fileLineSep + src.substring(pos-indentation, pos);
                gotSeparateLine = true;
              }
            }

            // Possibly add a leading space before the insertion
            if ((! gotSeparateLine) && (pos != 0)) {
              char precedingChar = src.charAt(pos-1);
              if (! (Character.isWhitespace(precedingChar)
                     // No space if it's the first formal or generic parameter
                     || precedingChar == '('
                     || precedingChar == '<')) {
                toInsert = " " + toInsert;
              }
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
            // add trailing whitespace
            // (test is not for "extends " because we just added a leading space, above)
            if ((! gotSeparateLine) && (! toInsert.startsWith(" extends "))) {
              toInsert = toInsert + " ";
            }
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

  /**
   * Removes the leading package.
   *
   * @return given <code>@com.foo.bar(baz)</code> it returns the pair
   * <code>{ com.foo, @bar(baz) }</code>.
   */
  private static Pair<String,String> removePackageInternal(String s) {
    int nameEnd = s.indexOf("(");
    if (nameEnd == -1) {
      nameEnd = s.length();
    }
    int dotIndex = s.lastIndexOf(".", nameEnd);
    if (dotIndex != -1) {
      String packageName = s.substring(0, nameEnd);
      if (packageName.startsWith("@")) {
        return Pair.of(packageName.substring(1),
                       "@" + s.substring(dotIndex + 1));
      } else {
        return Pair.of(packageName,
                       s.substring(dotIndex + 1));
      }
    } else {
      return Pair.of((String)null, s);
    }
  }

  private static Pattern extendsObjectPattern
    = Pattern.compile("^extends (.*) ((java\\.lang\\.)?Object)$");

  /**
   * Removes the leading package.
   * Handles "extends @B Object" and "extends @B java.lang.Object" strings.
   *
   * @return given <code>@com.foo.bar(baz)</code> it returns the pair
   * <code>{ com.foo, @bar(baz) }</code>.
   */
  public static Pair<String,String> removePackage(String s) {
    String extendsWrapped = null;
    Matcher m = extendsObjectPattern.matcher(s);
    if (m.matches()) {
      s = m.group(1);
      extendsWrapped = m.group(2);
    }
    Pair<String,String> result = removePackageInternal(s);
    // System.out.printf("removePackageInternal(%s) => %s%n", s, result);
    if (extendsWrapped != null) {
      return Pair.of(result.a, "extends " + result.b + " " + extendsWrapped);
    } else {
      return result;
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
