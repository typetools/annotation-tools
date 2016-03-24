package annotations.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import annotations.el.ABlock;
import annotations.el.AClass;
import annotations.el.ADeclaration;
import annotations.el.AElement;
import annotations.el.AExpression;
import annotations.el.AField;
import annotations.el.AMethod;
import annotations.el.AScene;
import annotations.el.ATypeElement;
import annotations.el.ATypeElementWithType;
import annotations.el.DefException;
import annotations.io.IndexFileParser;
import annotations.io.IndexFileWriter;
import annotations.util.coll.VivifyingMap;

/**
 * Containing class for classes implementing algebraic operations on scenes.
 *
 * @author dbro
 */
public class SceneOps {
  /**
   * Run an operation on a subcommand-specific number of JAIFs.
   * Currently the only available subcommand is "diff", which must be
   * the first of three arguments, followed in order by the "minuend"
   * and the "subtrahend" (see {@link Diff.apply(AScene, AScene}).
   */
  public static void main(String[] args) {
    assert args.length == 3 && "diff".equals(args[0]);
    AScene s1 = new AScene();
    AScene s2 = new AScene();

    try {
      IndexFileParser.parseFile(args[1], s1);
      IndexFileParser.parseFile(args[2], s2);
      AScene diff = Diff.apply(s1, s2);

      try (Writer w = new PrintWriter(System.out)){
        IndexFileWriter.write(diff, w);
      } catch (DefException e) {
        exitWithException(e);
      }
    } catch (IOException e) {
      exitWithException(e);
    }
  }

  /** Print stack trace and exit with return code 1. */
  private static void exitWithException(Exception e) {
    e.printStackTrace();
    System.exit(1);
  }

  // beginning of unit test set?
  public void testDiffEmpties() {
    assert new AScene().equals(Diff.apply(new AScene(), new AScene()));
  }
}

class Diff {
  private Diff() {}

  /**
   * Compute the difference of two scenes.
   *
   * @param minuend
   * @param subtrahend
   * @return a scene containing exactly the annotations that exist in
   * minuend but not in subtrahend
   */
  public static AScene apply(AScene minuend, AScene subtrahend) {
    AScene diff = new AScene();
    diff(minuend.packages, subtrahend.packages, diff.packages);
    diff(minuend.imports, subtrahend.imports, diff.imports);
    diff(minuend.classes, subtrahend.classes, diff.classes);
    diff.prune();
    return diff;
  }

  // The "diff" methods below all pass in two input arguments and an
  // output argument, which is necessary because the constructors of
  // AElement and subclasses are not public -- they must be created
  // "top-down" from an AScene.  Aside from the methods that take
  // Collection instances, these are all boilerplate that could be
  // abstracted away with some effort.  Each AElement subclass handles
  // the fields declared in its class definition, then calls the
  // superclass method to handle the remaining fields.

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static <K, V extends AElement> void diff(VivifyingMap<K, V> m,
      VivifyingMap<K, V> s, VivifyingMap<K, V> d) {
    if (m != null) {
      for (Map.Entry<K, V> e : m.entrySet()) {
        K key = e.getKey();
        V val = e.getValue();
        V mval = s.get(key);
        if (mval == null) {
          d.put(key, val);
        } else {
          diff(val, mval, d.vivify(key));
        }
      }
    }
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static <K, V> void diff(Map<K, Set<V>> m, Map<K, Set<V>> s,
      Map<K, Set<V>> d) {
    if (m != null) {
      for (Map.Entry<K, Set<V>> me : m.entrySet()) {
        K key = me.getKey();
        Set<V> val = me.getValue();
        Set<V> mval = s.get(key);
        if (mval == null) {
          d.put(key, val);
        } else {
          try {
            @SuppressWarnings("unchecked")
            Set<V> diff = (Set<V>) mval.getClass().newInstance();
            diff(val, mval, diff);
            if (!diff.isEmpty()) {
              d.put(key, diff);
            }
          } catch (InstantiationException e1) {
            e1.printStackTrace();
            System.exit(1);
          } catch (IllegalAccessException e1) {
            e1.printStackTrace();
            System.exit(1);
          }
        }
      }
    }
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static <T> void diff(Set<T> m, Set<T> s, Set<T> d) {
    if (m != null) {
      for (T t : m) {
        if (!s.contains(t)) {
          d.add(t);
        }
      }
    }
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  @SuppressWarnings("unused")
  private static void diff(AClass m, AClass s, AClass d) {
    diff(m.bounds, s.bounds, d.bounds);
    diff(m.extendsImplements, s.extendsImplements, d.extendsImplements);
    diff(m.methods, s.methods, d.methods);
    diff(m.staticInits, s.staticInits, d.staticInits);
    diff(m.instanceInits, s.instanceInits, d.instanceInits);
    diff(m.fields, s.fields, d.fields);
    diff(m.fieldInits, s.fieldInits, d.fieldInits);
    diff((ADeclaration) m, (ADeclaration) s, (ADeclaration) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  @SuppressWarnings("unused")
  private static void diff(AMethod m, AMethod s, AMethod d) {
    diff(m.bounds, s.bounds, d.bounds);
    diff(m.parameters, s.parameters, d.parameters);
    diff(m.throwsException, s.throwsException, d.throwsException);
    diff(m.parameters, s.parameters, d.parameters);
    diff(m.body, s.body, d.body);
    diff((ADeclaration) m, (ADeclaration) s, (ADeclaration) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  @SuppressWarnings("unused")
  private static void diff(AField m, AField s, AField d) {
    diff((ADeclaration) m, (ADeclaration) s, (ADeclaration) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static void diff(ABlock m, ABlock s, ABlock d) {
    diff(m.locals, s.locals, d.locals);
    diff((AExpression) m, (AExpression) s, (AExpression) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static void diff(AExpression m, AExpression s, AExpression d) {
    diff(m.typecasts, s.typecasts, d.typecasts);
    diff(m.instanceofs, s.instanceofs, d.instanceofs);
    diff(m.news, s.news, d.news);
    diff(m.calls, s.calls, d.calls);
    diff(m.refs, s.refs, d.refs);
    diff(m.funs, s.funs, d.funs);
    diff((AElement) m, (AElement) s, (AElement) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static void diff(ADeclaration m, ADeclaration s, ADeclaration d) {
    diff(m.insertAnnotations, s.insertAnnotations, d.insertAnnotations);
    diff(m.insertTypecasts, m.insertTypecasts, m.insertTypecasts);
    diff((AElement) m, (AElement) s, (AElement) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  @SuppressWarnings("unused")
  private static void diff(ATypeElementWithType m, ATypeElementWithType s,
      ATypeElementWithType d) {
    diff((ATypeElement) m, (ATypeElement) s, (ATypeElement) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static void diff(ATypeElement m, ATypeElement s, ATypeElement d) {
    diff(m.innerTypes, s.innerTypes, d.innerTypes);
    diff((AElement) m, (AElement) s, (AElement) d);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private static void diff(AElement m, AElement s, AElement d) {
    if (m != null && s != null) {
      diff(m.tlAnnotationsHere, s.tlAnnotationsHere, d.tlAnnotationsHere);
    }
  }
}
