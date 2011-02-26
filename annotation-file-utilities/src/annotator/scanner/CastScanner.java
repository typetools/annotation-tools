package annotator.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;

/** CastScanner stores information about the names and offsets of
 * casts inside a method, and can also be used to scan the source
 * tree and determine the index of a given cast, where the i^th
 * index corresponds to the i^th cast, using 0-based indexing.
 */
public class CastScanner extends CommonScanner {

  /**
   * Computes the index of the given cast tree amongst all cast trees inside
   * its method, using 0-based indexing.
   *
   * @param origpath the path ending in the given cast tree
   * @param tree the cast tree to search for
   * @return the index of the given cast tree
   */
  public static int indexOfCastTree(TreePath origpath, Tree tree) {
    TreePath path = findCountingContext(origpath);
    if (path == null) {
	  return -1;
    }

    CastScanner lvts = new CastScanner(tree);
    lvts.scan(path, null);
    return lvts.index;
  }

  private int index = -1;
  private boolean done = false;
  private Tree tree;

  private CastScanner(Tree tree) {
    this.index = -1;
    this.done = false;
    this.tree = tree;
  }

  @Override
  public Void visitTypeCast(TypeCastTree node, Void p) {
    if (!done) {
      index++;
    }
    if (tree == node) {
      done = true;
    }
    return p;
  }

  // Map from name of a method a list of bytecode offsets of all
  // casts in that method.
  private static Map<String,List<Integer>> methodNameToCastOffsets =
    new HashMap<String, List<Integer>>();


  /**
   * Adds a cast bytecode offset to the current list of offsets for
   * methodName.  This method must be called with monotonically increasing
   * offsets for any one method.
   *
   * @param methodName the name of the method
   * @param offset the offset to add
   */
  public static void addCastToMethod(String methodName, Integer offset) {
    List<  Integer> offsetList = methodNameToCastOffsets.get(methodName);
    if (offsetList == null) {
      offsetList = new ArrayList<  Integer>();
      methodNameToCastOffsets.put(methodName, offsetList);
    }
    offsetList.add(offset);
  }

  /**
   * Returns the index of the given offset within the list of offsets
   * for the given method, using 0-based indexing,
   * or returns a negative number if the offset is not one of the
   * offsets in the method.
   *
   * @param methodName the name of the method
   * @param offset the offset of the instanceof check
   * @return the index of the given offset, or a negative number if the
   *  given offset does not exists inside the method
   */
  public static Integer getMethodCastIndex(String methodName, Integer offset) {
    List<Integer> offsetList = methodNameToCastOffsets.get(methodName);
    if (offsetList == null) {
      return -1;
    }

    return offsetList.indexOf(offset);
  }
}
