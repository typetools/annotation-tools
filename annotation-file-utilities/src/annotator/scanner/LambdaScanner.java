package annotator.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plume.Pair;

import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class LambdaScanner extends CommonScanner {
  private static boolean debug = false;

  static Map<Pair<TreePath,Tree>, Integer> cache = new HashMap<Pair<TreePath,Tree>, Integer>();

  /**
   * Computes the index of the given lambda expression tree amongst all
   * new trees inside its method, using 0-based indexing. The tree has
   * to be a LambdaExpressionTree.
   *
   * @param origpath
   *            the path ending in the given lambda expression
   * @param tree
   *            the lambda expression to search for
   * @return the index of the given lambda expression
   */
  public static int indexOfLambda(TreePath origpath, Tree tree) {
      debug("indexOfLambda: " + origpath.getLeaf());

      Pair<TreePath,Tree> args = Pair.of(origpath, tree);
      if (cache.containsKey(args)) {
          return cache.get(args);
      }

      TreePath path = findCountingContext(origpath);
      if (path == null) {
          return -1;
      }

      LambdaScanner lvts = new LambdaScanner(tree);
      lvts.scan(path, null);
      cache.put(args, lvts.index);

      return lvts.index;
  }

  private int index = -1;
  private boolean done = false;
  private final Tree tree;

  private LambdaScanner(Tree tree) {
      this.index = -1;
      this.done = false;
      this.tree = tree;
  }

  @Override
  public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
      if (!done) {
          index++;
      }
      if (tree == node) {
          done = true;
      }
      return super.visitLambdaExpression(node, p);
  }

  public static void debug(String s) {
      if (debug) {
          System.out.println(s);
      }
  }

  private static Map<String, List<Integer>> methodNameToLambdaOffsets =
          new HashMap<String, List<Integer>>();

  public static void addNewToMethod(String methodName, Integer offset) {
      debug("adding lambda to method: " + methodName + " offset: " + offset);
      List<Integer> offsetList = methodNameToLambdaOffsets.get(methodName);
      if (offsetList == null) {
          offsetList = new ArrayList<Integer>();
          methodNameToLambdaOffsets.put(methodName, offsetList);
      }
      offsetList.add(offset);
  }

  public static Integer getMethodLambdaIndex(String methodName, Integer offset) {
      List<Integer> offsetList = methodNameToLambdaOffsets.get(methodName);
      if (offsetList == null) {
          throw new RuntimeException("LambdaScanner.getMethodLambdaIndex() : "
                  + "did not find offsets for method: " + methodName);
      }

      Integer offsetIndex = offsetList.indexOf(offset);
      if (offsetIndex < 0) {
          throw new RuntimeException("LambdaScanner.getMethodLambdaIndex() : "
                  + "in method: " + methodName + " did not find offset: "
                  + offset);
      }

      return offsetIndex;
  }
}
