package annotator.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/** InstanceOfScanner stores information about the names and offsets of
 * instanceof checks inside a method, and can also be used to scan the source
 * tree and determine the index of a given instanceof check, where the i^th
 * index corresponds to the i^th instanceof check, using 0-based indexing.
 */
public class InstanceOfScanner extends CommonScanner {

  /**
   * Computes the index of the given instanceof tree amongst all instanceof
   * tree inside its method, using 0-based indexing.
   *
   * @param path the path ending in the given instanceof tree
   * @param tree the instanceof tree to search for
   * @return the index of the given instanceof tree
   */
  public static int indexOfInstanceOfTree(TreePath path, Tree tree) {
    // only start searching from within this method
    path = findEnclosingMethod(path);
    if (path == null) {
      // Was called on something other than a local variable, so return
      // -1 to ensure that it doesn't match anything.
      return -1;
    }
    InstanceOfScanner ios = new InstanceOfScanner(tree);
    ios.scan(path, null);
    return ios.index;
  }

	public static int indexOfInstanceOfTreeInFieldInit(TreePath path, Tree tree) {
		// only start searching from within this field initializer
	    path = findEnclosingFieldInit(path);
	    if (path == null) {
	      return -1;
	    }

		InstanceOfScanner lvts = new InstanceOfScanner(tree);
		lvts.scan(path, null);
		return lvts.index;
	}

	public static int indexOfInstanceOfTreeInStaticInit(TreePath path, Tree tree) {
		// only start searching from within this method
	    path = findEnclosingStaticInit(path);
	    if (path == null) {
	      return -1;
	    }

	    InstanceOfScanner lvts = new InstanceOfScanner(tree);
		lvts.scan(path, null);
		return lvts.index;
	}

  
  private int index = -1;
  private boolean done = false;
  private Tree tree;

  /**
   * Creates an InstanceOfScanner that will scan the source tree for the
   *  given node representing the instanceof check to find.
   * @param tree the given instanceof check to search for
   */
  private InstanceOfScanner(Tree tree) {
    this.index = -1;
    this.done = false;
    this.tree = tree;
  }

  @Override
  public Void visitInstanceOf(InstanceOfTree node, Void p) {
    if (!done) {
      index++;
    }
    if (tree == node) {
      done = true;
    }
    return super.visitInstanceOf(node, p);
  }

  // Map from name of a method to a list of bytecode offsets of all
  // instanceof checks in that method.
  private static Map<String, List<Integer>> methodNameToInstanceOfOffsets =
    new HashMap<String, List<Integer>>();

  /**
   * Adds an instanceof bytecode offset to the current list of offsets for
   * methodName.  This method must be called with monotonically increasing
   * offsets for any one method.
   *
   * @param methodName the name of the method
   * @param offset the offset to add
   */
  public static void addInstanceOfToMethod(
        String methodName,
        Integer offset) {
     List<  Integer> offsetList =
      methodNameToInstanceOfOffsets.get(methodName);
    if (offsetList == null) {
      offsetList = new ArrayList<  Integer>();
      methodNameToInstanceOfOffsets.put(methodName, offsetList);
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
   * @return the index of the given offset, or a negative number
   *  if the offset does not exist inside the method
   */
  public static Integer getMethodInstanceOfIndex(
        String methodName,
        Integer offset)  {
     List<  Integer> offsetList =
      methodNameToInstanceOfOffsets.get(methodName);
    if (offsetList == null) {
      return -1;
//      throw new RuntimeException("InstanceOfScanner.getMethodCastIndex() : " +
//          "did not find offsets for method: " + methodName);
    }

    return offsetList.indexOf(offset);
  }
}
