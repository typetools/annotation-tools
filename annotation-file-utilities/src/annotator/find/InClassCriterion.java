package annotator.find;

import java.util.*;
import java.util.regex.*;

import javax.lang.model.element.Name;

import annotator.scanner.AnonymousClassScanner;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

// If there are dollar signs in a name, then there are two
// possibilities regarding how the dollar sign got there.
//  1. Inserted by the compiler, for inner classes.
//  2. Written by the programmer (or by a tool that creates .class files).
// We need to account for both possibilities (and all combinations of them).

// Example names
//   annotator.tests.FullClassName
//   annotator.tests.FullClassName$InnerClass
//   annotator.tests.FullClassName$0

/**
 * Represents the criterion that a program element is in a class with a
 * particular name.
 */
final class InClassCriterion implements Criterion {

  static boolean debug = false;

  public final String className;
  private final boolean exactMatch;

  /** The argument is a fully-qualified class name. */
  public InClassCriterion(String className, boolean exactMatch) {
    this.className = className;
    this.exactMatch = exactMatch;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Kind getKind() {
    return Kind.IN_CLASS;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    assert path == null || path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    return InClassCriterion.isSatisfiedBy(path, className, exactMatch);
  }

  static Pattern anonclassPattern;
  static {
    //for JDK 7: anonclassPattern = Pattern.compile("^(?<num>[0-9]+)(\\$(?<remaining>.*))?$");
    anonclassPattern = Pattern.compile("^([0-9]+)(\\$(.*))?$");
  }

  public static boolean isSatisfiedBy(TreePath path, String className, boolean exactMatch) {
    if (path == null)
      return false;

    // However much of the class name remains to match.
    String cname = className;

    // It is wrong to work from the leaf up to the root of the tree, which
    // would fail if the criterion is a.b.c and the actual is a.b.c.c.
    List<Tree> trees = new ArrayList<Tree>();
    for (Tree tree : path) {
      trees.add(tree);
    }
    Collections.reverse(trees);

    for (Tree tree : trees) {
      boolean checkAnon = false;

      switch (tree.getKind()) {
      case COMPILATION_UNIT:
        debug("InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);
        ExpressionTree packageTree = ((CompilationUnitTree) tree).getPackageName();
        if (packageTree == null) {
          // compilation unit is in default package; nothing to do
        } else {
          String declaredPackage = packageTree.toString();
          if (cname.startsWith(declaredPackage + ".")) {
            cname = cname.substring(declaredPackage.length()+1);
          } else {
            debug("false[COMPILATION_UNIT; bad declaredPackage = %s] InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", declaredPackage, cname, tree);
            return false;
          }
        }
        break;
      case CLASS:
      case INTERFACE:
      case ENUM:
      case ANNOTATION_TYPE:
        debug("InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);

        // all four Kinds are represented by ClassTree
        ClassTree c = (ClassTree)tree;
        Name csn = c.getSimpleName();
        
        if (csn == null) {
          debug("empty getSimpleName: InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);
          checkAnon = true;
          break;
        }
        String treeClassName = csn.toString();
        if (cname.equals(treeClassName)) {
          if (exactMatch) {
            cname = "";
          } else {
            debug("true InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);
            return true;
          }
        } else if (cname.startsWith(treeClassName + "$")
                   || (cname.startsWith(treeClassName + "."))) {
          cname = cname.substring(treeClassName.length()+1);
        } else {
          // System.out.println("cname else: " + cname);
          debug("false InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);
          return false;
        }
        break;
      case NEW_CLASS:
        // When matching the "new Class() { ... }" expression itself, we
        // should not use the anonymous class name.  But when matching
        // within the braces, we should.
        debug("InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);
        if (! cname.equals("")) {
          NewClassTree nc = (NewClassTree) tree;
          checkAnon = nc.getClassBody() != null;
        }
        break;
      default:
        // nothing to do
        break;
      }

      if (checkAnon) {
        // If block is anonymous class, and cname starts with an
        // anonymous class index, see if they match.

        Matcher anonclassMatcher = anonclassPattern.matcher(cname);
        if (! anonclassMatcher.matches()) {
          debug("false[anonclassMatcher] InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname, tree);
          return false;
        }
        //for JDK 7: String anonclassNumString = anonclassMatcher.group("num");
        //for JDK 7: cname = anonclassMatcher.group("remaining");
        String anonclassNumString = anonclassMatcher.group(1);
        cname = anonclassMatcher.group(3);
        if (cname == null) {
          cname = "";
        }
        int anonclassNum;
        try {
          anonclassNum = Integer.parseInt(anonclassNumString);
        } catch (NumberFormatException e) {
          throw new Error("This can't happen: " + cname + "$" + anonclassNumString, e);
        }

        int actualIndexInSource = AnonymousClassScanner.indexOfClassTree(path, tree);

        if (anonclassNum != actualIndexInSource) {
          debug("false[anonclassNum %d %d] InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", anonclassNum, actualIndexInSource, cname, tree);
          return false;
        }
      }
    }

    debug("%s InClassCriterion.isSatisfiedBy:%n  cname=%s%n  tree=%s%n", cname.equals(""), cname, path.getLeaf());
    return cname.equals("");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "In class '" + className + "'" + (exactMatch ? " (exactly)" : "");
  }

  /**
   * Return an array of Strings representing the characters between
   * successive instances of the delimiter character.
   * Always returns an array of length at least 1 (it might contain only the
   * empty string).
   * @see #split(String s, String delim)
   **/
  private static List<String> split(String s, char delim) {
    List<String> result = new ArrayList<String>();
    for (int delimpos = s.indexOf(delim); delimpos != -1; delimpos = s.indexOf(delim)) {
      result.add(s.substring(0, delimpos));
      s = s.substring(delimpos+1);
    }
    result.add(s);
    return result;
  }

  private static void debug(String message, Object... args) {
    if (debug) {
      System.out.printf(message, args);
    }
  }

}
