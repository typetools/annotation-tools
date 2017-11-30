package annotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plume.FileIOException;
import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Pair;
import plume.UtilMDE;
import scenelib.type.Type;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.ABlock;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.ADeclaration;
import scenelib.annotations.el.AElement;
import scenelib.annotations.el.AExpression;
import scenelib.annotations.el.AField;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.el.ATypeElement;
import scenelib.annotations.el.ATypeElementWithType;
import scenelib.annotations.el.AnnotationDef;
import scenelib.annotations.el.DefException;
import scenelib.annotations.el.ElementVisitor;
import scenelib.annotations.el.LocalLocation;
import scenelib.annotations.io.ASTIndex;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.ASTRecord;
import scenelib.annotations.io.DebugWriter;
import scenelib.annotations.io.IndexFileParser;
import scenelib.annotations.io.IndexFileWriter;
import scenelib.annotations.util.coll.VivifyingMap;
import annotator.find.AnnotationInsertion;
import annotator.find.CastInsertion;
import annotator.find.ConstructorInsertion;
import annotator.find.Criteria;
import annotator.find.GenericArrayLocationCriterion;
import annotator.find.Insertion;
import annotator.find.Insertions;
import annotator.find.NewInsertion;
import annotator.find.ReceiverInsertion;
import annotator.find.TreeFinder;
import annotator.find.TypedInsertion;
import annotator.scanner.LocalVariableScanner;
import annotator.specification.IndexFileSpecification;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;
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
 * Annotations that are not for the specified Java files are ignored.
 * <p>
 *
 * The <a name="command-line-options">command-line options</a> are as follows:
 * <!-- start options doc (DO NOT EDIT BY HAND) -->
 * <ul>
 *   <li id="optiongroup:General-options">General options
 *     <ul>
 *       <li id="option:outdir"><b>-d</b> <b>--outdir=</b><i>directory</i>. Directory in which output files are written. [default annotated/]</li>
 *       <li id="option:in-place"><b>-i</b> <b>--in-place=</b><i>boolean</i>. If true, overwrite original source files (making a backup first).
 *  Furthermore, if the backup files already exist, they are used instead
 *  of the .java files.  This behavior permits a user to tweak the .jaif
 *  file and re-run the annotator.
 *  <p>
 *
 *  Note that if the user runs the annotator with --in-place, makes edits,
 *  and then re-runs the annotator with this --in-place option, those
 *  edits are lost.  Similarly, if the user runs the annotator twice in a
 *  row with --in-place, only the last set of annotations will appear in
 *  the codebase at the end.
 *  <p>
 *
 *  To preserve changes when using the --in-place option, first remove the
 *  backup files.  Or, use the <tt>-d .</tt> option, which makes (and
 *  reads) no backup, instead of --in-place. [default false]</li>
 *       <li id="option:abbreviate"><b>-a</b> <b>--abbreviate=</b><i>boolean</i>. Abbreviate annotation names [default true]</li>
 *       <li id="option:comments"><b>-c</b> <b>--comments=</b><i>boolean</i>. Insert annotations in comments [default false]</li>
 *       <li id="option:omit-annotation"><b>-o</b> <b>--omit-annotation=</b><i>string</i>. Omit given annotation</li>
 *       <li id="option:nowarn"><b>--nowarn=</b><i>boolean</i>. Suppress warnings about disallowed insertions [default false]</li>
 *       <li id="option:convert-jaifs"><b>--convert-jaifs=</b><i>boolean</i>. Convert JAIFs to new format [default false]</li>
 *       <li id="option:help"><b>-h</b> <b>--help=</b><i>boolean</i>. Print usage information and exit [default false]</li>
 *     </ul>
 *   </li>
 *   <li id="optiongroup:Debugging-options">Debugging options
 *     <ul>
 *       <li id="option:verbose"><b>-v</b> <b>--verbose=</b><i>boolean</i>. Verbose (print progress information) [default false]</li>
 *       <li id="option:debug"><b>--debug=</b><i>boolean</i>. Debug (print debug information) [default false]</li>
 *       <li id="option:print-error-stack"><b>--print-error-stack=</b><i>boolean</i>. Print error stack [default false]</li>
 *     </ul>
 *   </li>
 * </ul>
 * <!-- end options doc -->
 */
public class Main {

  /** Directory in which output files are written. */
  @OptionGroup("General options")
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

  @Option("Suppress warnings about disallowed insertions")
  public static boolean nowarn;

  // Instead of doing insertions, create new JAIFs using AST paths
  //  extracted from existing JAIFs and source files they match
  @Option("Convert JAIFs to AST Path format")
  public static boolean convert_jaifs = false;

  @Option("-h Print usage information and exit")
  public static boolean help = false;

  // Debugging options go below here.

  @OptionGroup("Debugging options")
  @Option("-v Verbose (print progress information)")
  public static boolean verbose;

  @Option("Debug (print debug information)")
  public static boolean debug = false;

  @Option("Print error stack")
  public static boolean print_error_stack = false;

  // TODO: remove this.
  public static boolean temporaryDebug = false;

  private static ElementVisitor<Void, AElement> classFilter =
      new ElementVisitor<Void, AElement>() {
    <K, V extends AElement>
    Void filter(VivifyingMap<K, V> vm0, VivifyingMap<K, V> vm1) {
      for (Map.Entry<K, V> entry : vm0.entrySet()) {
        entry.getValue().accept(this, vm1.vivify(entry.getKey()));
      }
      return null;
    }

    @Override
    public Void visitAnnotationDef(AnnotationDef def, AElement el) {
      // not used, since package declarations not handled here
      return null;
    }

    @Override
    public Void visitBlock(ABlock el0, AElement el) {
      ABlock el1 = (ABlock) el;
      filter(el0.locals, el1.locals);
      return visitExpression(el0, el);
    }

    @Override
    public Void visitClass(AClass el0, AElement el) {
      AClass el1 = (AClass) el;
      filter(el0.methods, el1.methods);
      filter(el0.fields, el1.fields);
      filter(el0.fieldInits, el1.fieldInits);
      filter(el0.staticInits, el1.staticInits);
      filter(el0.instanceInits, el1.instanceInits);
      return visitDeclaration(el0, el);
    }

    @Override
    public Void visitDeclaration(ADeclaration el0, AElement el) {
      ADeclaration el1 = (ADeclaration) el;
      VivifyingMap<ASTPath, ATypeElement> insertAnnotations =
          el1.insertAnnotations;
      VivifyingMap<ASTPath, ATypeElementWithType> insertTypecasts =
          el1.insertTypecasts;
      for (Map.Entry<ASTPath, ATypeElement> entry :
          el0.insertAnnotations.entrySet()) {
        ASTPath p = entry.getKey();
        ATypeElement e = entry.getValue();
        insertAnnotations.put(p, e);
        // visitTypeElement(e, insertAnnotations.vivify(p));
      }
      for (Map.Entry<ASTPath, ATypeElementWithType> entry :
          el0.insertTypecasts.entrySet()) {
        ASTPath p = entry.getKey();
        ATypeElementWithType e = entry.getValue();
        scenelib.type.Type type = e.getType();
        if (type instanceof scenelib.type.DeclaredType
            && ((scenelib.type.DeclaredType) type).getName().isEmpty()) {
          insertAnnotations.put(p, e);
          // visitTypeElement(e, insertAnnotations.vivify(p));
        } else {
          insertTypecasts.put(p, e);
          // visitTypeElementWithType(e, insertTypecasts.vivify(p));
        }
      }
      return null;
    }

    @Override
    public Void visitExpression(AExpression el0, AElement el) {
      AExpression el1 = (AExpression) el;
      filter(el0.typecasts, el1.typecasts);
      filter(el0.instanceofs, el1.instanceofs);
      filter(el0.news, el1.news);
      return null;
    }

    @Override
    public Void visitField(AField el0, AElement el) {
      return visitDeclaration(el0, el);
    }

    @Override
    public Void visitMethod(AMethod el0, AElement el) {
      AMethod el1 = (AMethod) el;
      filter(el0.bounds, el1.bounds);
      filter(el0.parameters, el1.parameters);
      filter(el0.throwsException, el1.throwsException);
      el0.returnType.accept(this, el1.returnType);
      el0.receiver.accept(this, el1.receiver);
      el0.body.accept(this, el1.body);
      return visitDeclaration(el0, el);
    }

    @Override
    public Void visitTypeElement(ATypeElement el0, AElement el) {
      ATypeElement el1 = (ATypeElement) el;
      filter(el0.innerTypes, el1.innerTypes);
      return null;
    }

    @Override
    public Void visitTypeElementWithType(ATypeElementWithType el0,
        AElement el) {
      ATypeElementWithType el1 = (ATypeElementWithType) el;
      el1.setType(el0.getType());
      return visitTypeElement(el0, el);
    }

    @Override
    public Void visitElement(AElement el, AElement arg) {
      return null;
    }
  };

  private static AScene filteredScene(final AScene scene) {
    final AScene filtered = new AScene();
    filtered.packages.putAll(scene.packages);
    filtered.imports.putAll(scene.imports);
    for (Map.Entry<String, AClass> entry : scene.classes.entrySet()) {
      String key = entry.getKey();
      AClass clazz0 = entry.getValue();
      AClass clazz1 = filtered.classes.vivify(key);
      clazz0.accept(classFilter, clazz1);
    }
    filtered.prune();
    return filtered;
  }

  private static ATypeElement findInnerTypeElement(Tree t,
      ASTRecord rec, ADeclaration decl, Type type, Insertion ins) {
    ASTPath astPath = rec.astPath;
    GenericArrayLocationCriterion galc =
        ins.getCriteria().getGenericArrayLocation();
    assert astPath != null && galc != null;
    List<TypePathEntry> tpes = galc.getLocation();
    ASTPath.ASTEntry entry;
    for (TypePathEntry tpe : tpes) {
      switch (tpe.tag) {
      case ARRAY:
        if (!astPath.isEmpty()) {
          entry = astPath.getLast();
          if (entry.getTreeKind() == Tree.Kind.NEW_ARRAY
              && entry.childSelectorIs(ASTPath.TYPE)) {
            entry = new ASTPath.ASTEntry(Tree.Kind.NEW_ARRAY,
                ASTPath.TYPE, entry.getArgument() + 1);
            break;
          }
        }
        entry = new ASTPath.ASTEntry(Tree.Kind.ARRAY_TYPE,
            ASTPath.TYPE);
        break;
      case INNER_TYPE:
        entry = new ASTPath.ASTEntry(Tree.Kind.MEMBER_SELECT,
            ASTPath.EXPRESSION);
        break;
      case TYPE_ARGUMENT:
        entry = new ASTPath.ASTEntry(Tree.Kind.PARAMETERIZED_TYPE,
            ASTPath.TYPE_ARGUMENT, tpe.arg);
        break;
      case WILDCARD:
        entry = new ASTPath.ASTEntry(Tree.Kind.UNBOUNDED_WILDCARD,
            ASTPath.BOUND);
        break;
      default:
        throw new IllegalArgumentException("unknown type tag " + tpe.tag);
      }
      astPath = astPath.extend(entry);
    }

    return decl.insertAnnotations.vivify(astPath);
  }

  private static void convertInsertion(String pkg,
      JCTree.JCCompilationUnit tree, ASTRecord rec, Insertion ins,
      AScene scene, Multimap<Insertion, Annotation> insertionSources) {
    Collection<Annotation> annos = insertionSources.get(ins);
    if (rec == null) {
      if (ins.getCriteria().isOnPackage()) {
        for (Annotation anno : annos) {
          scene.packages.get(pkg).tlAnnotationsHere.add(anno);
        }
      }
    } else if (scene != null && rec.className != null) {
      AClass clazz = scene.classes.vivify(rec.className);
      ADeclaration decl = null;  // insertion target
      if (ins.getCriteria().onBoundZero()) {
        int n = rec.astPath.size();
        if (!rec.astPath.get(n-1).childSelectorIs(ASTPath.BOUND)) {
          ASTPath astPath = ASTPath.empty();
          for (int i = 0; i < n; i++) {
            astPath = astPath.extend(rec.astPath.get(i));
          }
          astPath = astPath.extend(
              new ASTPath.ASTEntry(Tree.Kind.TYPE_PARAMETER,
                  ASTPath.BOUND, 0));
          rec = rec.replacePath(astPath);
        }
      }
      if (rec.methodName == null) {
        decl = rec.varName == null ? clazz
            : clazz.fields.vivify(rec.varName);
      } else {
        AMethod meth = clazz.methods.vivify(rec.methodName);
        if (rec.varName == null) {
          decl = meth;  // ?
        } else {
          try {
            int i = Integer.parseInt(rec.varName);
            decl = i < 0 ? meth.receiver
                : meth.parameters.vivify(i);
          } catch (NumberFormatException e) {
            TreePath path = ASTIndex.getTreePath(tree, rec);
            JCTree.JCVariableDecl varTree = null;
            JCTree.JCMethodDecl methTree = null;
            JCTree.JCClassDecl classTree = null;
            loop:
              while (path != null) {
                Tree leaf = path.getLeaf();
                switch (leaf.getKind()) {
                case VARIABLE:
                  varTree = (JCTree.JCVariableDecl) leaf;
                  break;
                case METHOD:
                  methTree = (JCTree.JCMethodDecl) leaf;
                  break;
                case ANNOTATION:
                case CLASS:
                case ENUM:
                case INTERFACE:
                  break loop;
                default:
                  path = path.getParentPath();
                }
              }
            while (path != null) {
              Tree leaf = path.getLeaf();
              Tree.Kind kind = leaf.getKind();
              if (kind == Tree.Kind.METHOD) {
                methTree = (JCTree.JCMethodDecl) leaf;
                int i = LocalVariableScanner.indexOfVarTree(path,
                    varTree, rec.varName);
                int m = methTree.getStartPosition();
                int a = varTree.getStartPosition();
                int b = varTree.getEndPosition(tree.endPositions);
                LocalLocation loc = new LocalLocation(i, a-m, b-a);
                decl = meth.body.locals.vivify(loc);
                break;
              }
              if (ASTPath.isClassEquiv(kind)) {
                classTree = (JCTree.JCClassDecl) leaf;
                // ???
                    break;
              }
              path = path.getParentPath();
            }
          }
        }
      }
      if (decl != null) {
        AElement el;
        if (rec.astPath.isEmpty()) {
          el = decl;
        } else if (ins.getKind() == Insertion.Kind.CAST) {
          scenelib.annotations.el.ATypeElementWithType elem =
              decl.insertTypecasts.vivify(rec.astPath);
          elem.setType(((CastInsertion) ins).getType());
          el = elem;
        } else {
          el = decl.insertAnnotations.vivify(rec.astPath);
        }
        for (Annotation anno : annos) {
          el.tlAnnotationsHere.add(anno);
        }
        if (ins instanceof TypedInsertion) {
          TypedInsertion ti = (TypedInsertion) ins;
          if (!rec.astPath.isEmpty()) {
            // addInnerTypePaths(decl, rec, ti, insertionSources);
          }
          for (Insertion inner : ti.getInnerTypeInsertions()) {
            Tree t = ASTIndex.getNode(tree, rec);
            if (t != null) {
              ATypeElement elem = findInnerTypeElement(t,
                  rec, decl, ti.getType(), inner);
              for (Annotation a : insertionSources.get(inner)) {
                elem.tlAnnotationsHere.add(a);
              }
            }
          }
        }
      }
    }
  }


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
                        scenelib.annotations.io.classfile.ClassFileReader.INDEX_UTILS_VERSION);
    }

    Options options = new Options(
        "Main [options] { jaif-file | java-file | @arg-file } ...\n"
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

    DebugWriter dbug = new DebugWriter();
    DebugWriter verb = new DebugWriter();
    DebugWriter both = dbug.or(verb);
    dbug.setEnabled(debug);
    verb.setEnabled(verbose);
    TreeFinder.warn.setEnabled(!nowarn);
    TreeFinder.stak.setEnabled(print_error_stack);
    TreeFinder.dbug.setEnabled(debug);
    Criteria.dbug.setEnabled(debug);

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

    // Indices to maintain insertion source traces.
    Map<String, Multimap<Insertion, Annotation>> insertionIndex =
        new HashMap<String, Multimap<Insertion, Annotation>>();
    Map<Insertion, String> insertionOrigins = new HashMap<Insertion, String>();
    Map<String, AScene> scenes = new HashMap<String, AScene>();

    // maintain imports info for annotations field
    // Key: fully-qualified annotation name. e.g. "com.foo.Bar" for annotation @com.foo.Bar(x).
    // Value: names of packages this annotation needs.
    Map<String, Set<String>> annotationImports = new HashMap<>();

    IndexFileParser.setAbbreviate(abbreviate);
    for (String arg : file_args) {
      if (arg.endsWith(".java")) {
        javafiles.add(arg);
      } else if (arg.endsWith(".jaif") ||
                 arg.endsWith(".jann")) {
        IndexFileSpecification spec = new IndexFileSpecification(arg);
        try {
          List<Insertion> parsedSpec = spec.parse();
          if (temporaryDebug) {
            System.out.printf("parsedSpec (size %d):%n", parsedSpec.size());
            for (Insertion insertion : parsedSpec) {
              System.out.printf("  %s, isInserted=%s%n", insertion, insertion.isInserted());
            }
          }
          AScene scene = spec.getScene();
          Collections.sort(parsedSpec, new Comparator<Insertion>() {
            @Override
            public int compare(Insertion i1, Insertion i2) {
              ASTPath p1 = i1.getCriteria().getASTPath();
              ASTPath p2 = i2.getCriteria().getASTPath();
              return p1 == null
                  ? p2 == null ? 0 : -1
                  : p2 == null ? 1 : p1.compareTo(p2);
            }
          });
          if (convert_jaifs) {
            scenes.put(arg, filteredScene(scene));
            for (Insertion ins : parsedSpec) {
              insertionOrigins.put(ins, arg);
            }
            if (!insertionIndex.containsKey(arg)) {
              insertionIndex.put(arg,
                  LinkedHashMultimap.<Insertion, Annotation>create());
            }
            insertionIndex.get(arg).putAll(spec.insertionSources());
          }
          both.debug("Read %d annotations from %s%n", parsedSpec.size(), arg);
          if (omit_annotation != null) {
            List<Insertion> filtered =
                new ArrayList<Insertion>(parsedSpec.size());
            for (Insertion insertion : parsedSpec) {
              // TODO: this won't omit annotations if the insertion is more than
              // just the annotation (such as if the insertion is a cast
              // insertion or a 'this' parameter in a method declaration).
              if (! omit_annotation.equals(insertion.getText())) {
                filtered.add(insertion);
              }
            }
            parsedSpec = filtered;
            both.debug("After filtering: %d annotations from %s%n",
                parsedSpec.size(), arg);
          }
          insertions.addAll(parsedSpec);
          annotationImports.putAll(spec.annotationImports());
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
          if (print_error_stack) {
            e.printStackTrace();
          }
          System.exit(1);
        }
      } else {
        throw new Error("Unrecognized file extension: " + arg);
      }
    }

    if (dbug.isEnabled()) {
      dbug.debug("In annotator.Main:%n");
      dbug.debug("%d insertions, %d .java files%n",
          insertions.size(), javafiles.size());
      dbug.debug("Insertions:%n");
      for (Insertion insertion : insertions) {
        dbug.debug("  %s, isInserted=%s%n", insertion, insertion.isInserted());
      }
    }

    for (String javafilename : javafiles) {
      verb.debug("Processing %s%n", javafilename);

      File javafile = new File(javafilename);
      File unannotated = new File(javafilename + ".unannotated");
      if (in_place) {
        // It doesn't make sense to check timestamps;
        // if the .java.unannotated file exists, then just use it.
        // A user can rename that file back to just .java to cause the
        // .java file to be read.
        if (unannotated.exists()) {
          verb.debug("Renaming %s to %s%n", unannotated, javafile);
          boolean success = unannotated.renameTo(javafile);
          if (! success) {
            throw new Error(String.format("Failed renaming %s to %s",
                                          unannotated, javafile));
          }
        }
      }

      String fileSep = System.getProperty("file.separator");
      String fileLineSep = System.getProperty("line.separator");
      Source src;
      // Get the source file, and use it to obtain parse trees.
      try {
        // fileLineSep is set here so that exceptions can be caught
        fileLineSep = UtilMDE.inferLineSeparator(javafilename);
        src = new Source(javafilename);
        verb.debug("Parsed %s%n", javafilename);
      } catch (Source.CompilerException e) {
        e.printStackTrace();
        return;
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      // Imports required to resolve annotations (when abbreviate==true).
      LinkedHashSet<String> imports = new LinkedHashSet<String>();
      int num_insertions = 0;
      String pkg = "";

      for (CompilationUnitTree cut : src.parse()) {
        JCTree.JCCompilationUnit tree = (JCTree.JCCompilationUnit) cut;
        ExpressionTree pkgExp = cut.getPackageName();
        pkg = pkgExp == null ? "" : pkgExp.toString();

        // Create a finder, and use it to get positions.
        TreeFinder finder = new TreeFinder(tree);
        SetMultimap<Pair<Integer, ASTPath>, Insertion> positions =
            finder.getPositions(tree, insertions);
        if (dbug.isEnabled()) {
          dbug.debug("In annotator.Main:%n");
          dbug.debug("positions (for %d insertions) = %s%n",
                     insertions.size(), positions);
        }

        if (convert_jaifs) {
          // program used only for JAIF conversion; execute following
          // block and then skip remainder of loop
          Multimap<ASTRecord, Insertion> astInsertions = finder.getPaths();
          for (Map.Entry<ASTRecord, Collection<Insertion>> entry :
              astInsertions.asMap().entrySet()) {
            ASTRecord rec = entry.getKey();
            for (Insertion ins : entry.getValue()) {
              if (ins.getCriteria().getASTPath() != null) { continue; }
              String arg = insertionOrigins.get(ins);
              AScene scene = scenes.get(arg);
              Multimap<Insertion, Annotation> insertionSources =
                  insertionIndex.get(arg);
              // String text =
              //  ins.getText(comments, abbreviate, false, 0, '\0');

              // TODO: adjust for missing end of path (?)

              if (insertionSources.containsKey(ins)) {
                convertInsertion(pkg, tree, rec, ins, scene, insertionSources);
              }
            }
          }
          continue;
        }

        // Apply the positions to the source file.
        if (both.isEnabled()) {
          System.err.printf(
              "getPositions returned %d positions in tree for %s%n",
              positions.size(), javafilename);
        }

        Set<Pair<Integer, ASTPath>> positionKeysUnsorted =
            positions.keySet();
        Set<Pair<Integer, ASTPath>> positionKeysSorted =
          new TreeSet<Pair<Integer, ASTPath>>(
              new Comparator<Pair<Integer, ASTPath>>() {
                @Override
                public int compare(Pair<Integer, ASTPath> p1,
                    Pair<Integer, ASTPath> p2) {
                  int c = Integer.compare(p2.a, p1.a);
                  if (c == 0) {
                    c = p2.b == null ? p1.b == null ? 0 : -1
                        : p1.b == null ? 1 : p2.b.compareTo(p1.b);
                  }
                  return c;
                }
              });
        positionKeysSorted.addAll(positionKeysUnsorted);
        for (Pair<Integer, ASTPath> pair : positionKeysSorted) {
          boolean receiverInserted = false;
          boolean newInserted = false;
          boolean constructorInserted = false;
          Set<String> seen = new TreeSet<String>();
          List<Insertion> toInsertList = new ArrayList<Insertion>(positions.get(pair));
          Collections.reverse(toInsertList);
          dbug.debug("insertion pos: %d%n", pair.a);
          assert pair.a >= 0
            : "pos is negative: " + pair.a + " " + toInsertList.get(0) + " " + javafilename;
          for (Insertion iToInsert : toInsertList) {
            // Possibly add whitespace after the insertion
            String trailingWhitespace = "";
            boolean gotSeparateLine = false;
            int pos = pair.a;  // reset each iteration in case of dyn adjustment
            if (iToInsert.isSeparateLine()) {
              // System.out.printf("isSeparateLine=true for insertion at pos %d: %s%n", pos, iToInsert);
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

            if (iToInsert.getKind() == Insertion.Kind.ANNOTATION) {
              AnnotationInsertion ai = (AnnotationInsertion) iToInsert;
              if (ai.isGenerateBound()) {  // avoid multiple ampersands
                try {
                  String s = src.substring(pos, pos+9);
                  if ("Object & ".equals(s)) {
                    ai.setGenerateBound(false);
                    precedingChar = '.';  // suppress leading space
                  }
                } catch (StringIndexOutOfBoundsException e) {}
              }
              if (ai.isGenerateExtends()) {  // avoid multiple "extends"
                try {
                  String s = src.substring(pos, pos+9);
                  if (" extends ".equals(s)) {
                    ai.setGenerateExtends(false);
                    pos += 8;
                  }
                } catch (StringIndexOutOfBoundsException e) {}
              }
            } else if (iToInsert.getKind() == Insertion.Kind.CAST) {
                ((CastInsertion) iToInsert)
                        .setOnArrayLiteral(src.charAt(pos) == '{');
            } else if (iToInsert.getKind() == Insertion.Kind.RECEIVER) {
              ReceiverInsertion ri = (ReceiverInsertion) iToInsert;
              ri.setAnnotationsOnly(receiverInserted);
              receiverInserted = true;
            } else if (iToInsert.getKind() == Insertion.Kind.NEW) {
              NewInsertion ni = (NewInsertion) iToInsert;
              ni.setAnnotationsOnly(newInserted);
              newInserted = true;
            } else if (iToInsert.getKind() == Insertion.Kind.CONSTRUCTOR) {
              ConstructorInsertion ci = (ConstructorInsertion) iToInsert;
              if (constructorInserted) { ci.setAnnotationsOnly(true); }
              constructorInserted = true;
            }

            String toInsert = iToInsert.getText(comments, abbreviate,
                gotSeparateLine, pos, precedingChar) + trailingWhitespace;
            if (seen.contains(toInsert)) { continue; }  // eliminate duplicates
            seen.add(toInsert);

            // If it's already there, don't re-insert.  This is a hack!
            // Also, I think this is already checked when constructing the
            // insertions.
            int precedingTextPos = pos-toInsert.length()-1;
            if (precedingTextPos >= 0) {
              String precedingTextPlusChar
                = src.getString().substring(precedingTextPos, pos);
              if (toInsert.equals(
                      precedingTextPlusChar.substring(0, toInsert.length()))
                  || toInsert.equals(precedingTextPlusChar.substring(1))) {
                dbug.debug(
                    "Inserting '%s' at %d in code of length %d with preceding text '%s'%n",
                    toInsert, pos, src.getString().length(),
                    precedingTextPlusChar);
                dbug.debug("Already present, skipping%n");
                continue;
              }
            }

            // TODO: Neither the above hack nor this check should be
            // necessary.  Find out why re-insertions still occur and
            // fix properly.
            if (iToInsert.isInserted()) { continue; }
            src.insert(pos, toInsert);
            if (verbose && !debug) {
              System.out.print(".");
              num_insertions++;
              if ((num_insertions % 50) == 0) {
                System.out.println();   // terminate the line that contains dots
              }
            }
            dbug.debug("Post-insertion source: %n" + src.getString());

            Set<String> packageNames = iToInsert.getPackageNames();
            if (!packageNames.isEmpty()) {
              dbug.debug("Need import %s%n  due to insertion %s%n",
                  packageNames, toInsert);
              imports.addAll(packageNames);
            }
            if (iToInsert instanceof AnnotationInsertion) {
              AnnotationInsertion annoToInsert = (AnnotationInsertion) iToInsert;
              Set<String> annoImports = annotationImports.get(annoToInsert.getAnnotationFullyQualifiedName());
              if (annoImports != null) {
                imports.addAll(annoImports);
              }
            }
          }
        }
      }

      if (convert_jaifs) {
        for (Map.Entry<String, AScene> entry : scenes.entrySet()) {
          String filename = entry.getKey();
          AScene scene = entry.getValue();
          try {
            IndexFileWriter.write(scene, filename + ".converted");
          } catch (DefException e) {
            System.err.println(filename + ": " + " format error in conversion");
            if (print_error_stack) {
              e.printStackTrace();
            }
          }
        }
        return;  // done with conversion
      }

      if (dbug.isEnabled()) {
        dbug.debug("%d imports to insert%n", imports.size());
        for (String classname : imports) {
          dbug.debug("  %s%n", classname);
        }
      }

      // insert import statements
      {
        Pattern importPattern = Pattern.compile("(?m)^import\\b");
        Pattern packagePattern = Pattern.compile("(?m)^package\\b.*;(\\n|\\r\\n?)");
        int importIndex = 0;      // default: beginning of file
        String srcString = src.getString();
        Matcher m = importPattern.matcher(srcString);
        Set<String> inSource = new TreeSet<String>();
        if (m.find()) {
          importIndex = m.start();
          do {
            int i = m.start();
            int j = srcString.indexOf(System.lineSeparator(), i) + 1;
            if (j <= 0) {
              j = srcString.length();
            }
            String s = srcString.substring(i, j);
            inSource.add(s);
          } while (m.find());
        } else {
          // Debug.info("Didn't find import in " + srcString);
          m = packagePattern.matcher(srcString);
          if (m.find()) {
            importIndex = m.end();
          }
        }
        for (String classname : imports) {
          String toInsert = "import " + classname + ";" + fileLineSep;
          if (!inSource.contains(toInsert)) {
            inSource.add(toInsert);
            src.insert(importIndex, toInsert);
            importIndex += toInsert.length();
          }
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

  /** Return the representation of the leaf of the path. */
  public static String leafString(TreePath path) {
    if (path == null) {
      return "null";
    }
    return treeToString(path.getLeaf());
  }

  /** Return the first non-empty line of the tree's printed representation. */
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
