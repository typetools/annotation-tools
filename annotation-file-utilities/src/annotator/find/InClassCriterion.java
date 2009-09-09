package annotator.find;

import java.util.*;

import annotator.scanner.ClassScanner;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element is in a class with a
 * particular name.
 */
final class InClassCriterion implements Criterion {

  // does include package
  // either:
  //  annotator.tests.FullClassName
  //  annotator.tests.FullClassName$InnerClass
  //  annotator.tests.FullClassName$0
  private String className;
  private String packageName;
  private List<String> listOfClassNames;

  public InClassCriterion(String className) {
    if (className.contains(".")) {
      this.packageName = className.substring(0, className.lastIndexOf("."));
      this.className = className.substring(className.lastIndexOf(".")+1);
    } else {
      this.packageName = "";
      this.className = className;
    }

    // If there are dollar signs in a name, then there are two
    // possibilities regarding how the dollar sign got there.
    //  1. Inserted by the compiler, for inner classes.
    //  2. Written by the programmer (or by a tool that creates .class files).
    // We need to account for both possibilities (and all combinations of them).

    listOfClassNames = split(this.className, '$');
  }

  /**
   * {@inheritDoc}
   */
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

    if (path == null)
      return false;

    List<String> remainingClassNamesToMatch =
      new ArrayList<String>(listOfClassNames);

    // This loop works from the leaf up to the root of the tree.
    do {
      Tree tree = path.getLeaf();
      boolean checkAnon = false;
      switch (tree.getKind()) {
      case COMPILATION_UNIT:
        ExpressionTree packageTree = ((CompilationUnitTree) tree).getPackageName();
        if (packageTree == null) { // compilation unit is in default package
          return remainingClassNamesToMatch.isEmpty();
        }
        String declaredPackage = packageTree.toString();
        boolean b = (remainingClassNamesToMatch.isEmpty()
                     && declaredPackage.equals(packageName));
        return b;
        // unreachable: break;
      case CLASS:
        ClassTree c = (ClassTree)tree;
        String lowestClassName = c.getSimpleName().toString();
        if (remainingClassNamesToMatch.isEmpty()) {
          return false;
        }
        int lastIndex = remainingClassNamesToMatch.size() - 1;
        String lastClassNameToMatch =
          remainingClassNamesToMatch.get(lastIndex).trim();
        // True if name has length 0 (why?) or is a number
        checkAnon = (lastClassNameToMatch.isEmpty()
                     || (parseInt(lastClassNameToMatch) != null));

        if (checkAnon) {
          // This seems to be adding a duplicate.  Why?
          remainingClassNamesToMatch.add(lastClassNameToMatch);
        } else {
          List<String> lowestParts = split(lowestClassName, '$');
          int idx = Collections.lastIndexOfSubList(remainingClassNamesToMatch, lowestParts);
          if (idx != remainingClassNamesToMatch.size() - lowestParts.size()) {
            return false;
          }
          for (String lowestPart : lowestParts) {
            assert lowestPart.equals(remainingClassNamesToMatch.get(idx));
            remainingClassNamesToMatch.remove(idx);
          }
        }
        break;
      case NEW_CLASS:
        NewClassTree nc = (NewClassTree) tree;
        checkAnon = nc.getClassBody() != null;
        break;
      default:
        // nothing to do
        break;
      }

      if (checkAnon) {
        // if block is anonymous class, and last number is an
        // anonymous class index, see if they match
        int lastIndex = remainingClassNamesToMatch.size() - 1;
        String lastClassNameToMatch =
          remainingClassNamesToMatch.get(lastIndex);
        //        remainingClassNamesToMatch.add(lastIndex, "");
        remainingClassNamesToMatch.remove(lastIndex);

        Integer lastIndexToMatch = null;
        try {
          lastIndexToMatch = Integer.parseInt(lastClassNameToMatch);
        } catch(Exception e) {
          return false;
        }

        if (lastIndexToMatch != null) {
          Integer actualIndexInSource = ClassScanner.indexOfClassTree(path, tree);

          boolean b= lastIndexToMatch.equals(actualIndexInSource);
          if (!b) {
            return false;
          }
        }
      }
      // TODO: what if tree isn't ClassTree, but rather anonymous class
      // and so corresponding name is Class$0?
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return "In package: " + packageName + " in class '" + className + "'";
  }

  private Integer parseInt(String s) {
    Integer i = null;
    try {
    i = Integer.parseInt(s);
    } catch(Exception e) {

    }
    return i;
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

}
