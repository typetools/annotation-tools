package annotator.find;

import java.util.ArrayList;
import java.util.List;

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

    listOfClassNames = new ArrayList<String>();
    // String you want to split against is just the single character: $
    // The regex is: \$
    // Of course, you need to escape the first backslash in the .java file
    for (String s : this.className.split("\\$")) {
      listOfClassNames.add(s);
    }
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


    Tree tree = path.getLeaf();

    do {
      tree = path.getLeaf();
      boolean checkAnon = false;
      if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
        ExpressionTree packageTree = ((CompilationUnitTree) tree).getPackageName();
        if (packageTree == null) { // compilation unit is in default package
        return remainingClassNamesToMatch.isEmpty();
        }
        String declaredPackage = packageTree.toString();
        boolean b= remainingClassNamesToMatch.isEmpty() &&
        declaredPackage.equals(packageName);
        return b;
      }
      if (tree.getKind() == Tree.Kind.CLASS) {
        ClassTree c = (ClassTree)tree;
        String lowestClassName = c.getSimpleName().toString();
        if (remainingClassNamesToMatch.isEmpty()) {
          return false;
        }
        int lastIndex = remainingClassNamesToMatch.size() - 1;
        String lastClassNameToMatch =
          remainingClassNamesToMatch.get(lastIndex).trim();
        checkAnon = lastClassNameToMatch.isEmpty() ||
        (parseInt(lastClassNameToMatch) != null);

        if (checkAnon) {
          remainingClassNamesToMatch.add(lastClassNameToMatch);
        }
        if (!checkAnon && !lowestClassName.equals(lastClassNameToMatch)) {
          return false;
        }

        if (!checkAnon) {
          remainingClassNamesToMatch.remove(lastIndex);
        }
      }
      if (tree.getKind() == Tree.Kind.NEW_CLASS) {
        NewClassTree nc = (NewClassTree) tree;
        checkAnon = nc.getClassBody() != null;
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
}
