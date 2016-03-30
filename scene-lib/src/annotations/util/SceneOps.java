package annotations.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import com.sun.tools.javac.util.Pair;

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
import annotations.el.AnnotationDef;
import annotations.el.DefException;
import annotations.el.ElementVisitor;
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
   * and the "subtrahend" (see {@link #diff(AScene, AScene)}.
   *
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    assert args.length == 3 && "diff".equals(args[0]);
    AScene s1 = new AScene();
    AScene s2 = new AScene();

    try {
      IndexFileParser.parseFile(args[1], s1);
      IndexFileParser.parseFile(args[2], s2);
      AScene diff = diff(s1, s2);

      try (Writer w = new PrintWriter(System.out)) {
        IndexFileWriter.write(diff, w);
      } catch (DefException e) {
        exitWithException(e);
      }
    } catch (IOException e) {
      exitWithException(e);
    }
  }

  /**
   * @param s1
   * @param s2
   * @return s1 - s2 ("set difference")
   */
  public static AScene diff(AScene s1, AScene s2) {
    AScene diff = new AScene();
    new DiffVisitor().visitScene(s1, s2, diff);
    diff.prune();
    return diff;
  }

  /** Print stack trace and exit with return code 1. */
  private static void exitWithException(Exception e) {
    e.printStackTrace();
    System.exit(1);
  }

  // beginning of unit test set?
  public static void testDiffEmpties() {
    assert new AScene().equals(diff(new AScene(), new AScene()));
  }
  public static void testDiffSame() throws IOException {
    AScene scene1 = new AScene();
    AScene scene2 = new AScene();
    String dirname =
        "/homes/gws/dbro/src/jsr308/annotation-tools/scene-lib/test/annotations/tests/classfile/cases";
    String[] cases = { "ClassEmpty", "ClassNonEmpty", "FieldGeneric",
        "FieldSimple", "LocalVariableGenericArray", "MethodReceiver",
        "MethodReturnTypeGenericArray", "ObjectCreationGenericArray",
        "ObjectCreation", "TypecastGenericArray", "Typecast" };
    for (int i = 0; i < cases.length; i++) {
      String filename = dirname+"/Test"+cases[i]+".jaif";
      IndexFileParser.parseFile(filename, scene1);
      IndexFileParser.parseFile(filename, scene2);
      boolean b1 = new AScene().equals(diff(scene1, scene1));
      boolean b2 = new AScene().equals(diff(scene1, scene2));
      System.err.print(cases[i]+": ");
      System.err.print(b1 ? "yes! " : "no! ");
      System.err.println(b2 ? "yes!" : "no!");
    }
  }
}

/**
 * Visitor for calculating "set difference" of scenes.
 *
 * @author dbro
 */
class DiffVisitor implements ElementVisitor<Void, Pair<AElement, AElement>> {
  /**
   * Compute the difference of two scenes, that is, a scene containing
   * insertion specifications that exist in the first but not in the
   * second.
   *
   * @param minuend
   * @param subtrahend
   * @param diff scene to be filled in with the difference of minuend
   * and subtrahend
   */
  public void visitScene(AScene minuend, AScene subtrahend, AScene diff) {
    visitElements(minuend.packages, subtrahend.packages, diff.packages);
    diff(minuend.imports, subtrahend.imports, diff.imports);
    visitElements(minuend.classes, subtrahend.classes, diff.classes);
  }

  // Never used, as annotations and definitions don't get duplicated.
  @Override
  public Void visitAnnotationDef(AnnotationDef m,
      Pair<AElement, AElement> arg) {
    return null;
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitBlock(ABlock m, Pair<AElement, AElement> arg) {
    ABlock s = (ABlock) arg.fst;
    ABlock d = (ABlock) arg.snd;
    visitElements(m.locals, s.locals, d.locals);
    return visitExpression(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitClass(AClass m, Pair<AElement, AElement> arg) {
    AClass s = (AClass) arg.fst;
    AClass d = (AClass) arg.snd;
    visitElements(m.bounds, s.bounds, d.bounds);
    visitElements(m.extendsImplements, s.extendsImplements,
        d.extendsImplements);
    visitElements(m.methods, s.methods, d.methods);
    visitElements(m.staticInits, s.staticInits, d.staticInits);
    visitElements(m.instanceInits, s.instanceInits, d.instanceInits);
    visitElements(m.fields, s.fields, d.fields);
    visitElements(m.fieldInits, s.fieldInits, d.fieldInits);
    return visitDeclaration(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitDeclaration(ADeclaration m, Pair<AElement, AElement> arg) {
    ADeclaration s = (ADeclaration) arg.fst;
    ADeclaration d = (ADeclaration) arg.snd;
    visitElements(m.insertAnnotations, s.insertAnnotations,
        d.insertAnnotations);
    visitElements(m.insertTypecasts, m.insertTypecasts, m.insertTypecasts);
    return visitElement(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitExpression(AExpression m, Pair<AElement, AElement> arg) {
    AExpression s = (AExpression) arg.fst;
    AExpression d = (AExpression) arg.snd;
    visitElements(m.typecasts, s.typecasts, d.typecasts);
    visitElements(m.instanceofs, s.instanceofs, d.instanceofs);
    visitElements(m.news, s.news, d.news);
    visitElements(m.calls, s.calls, d.calls);
    visitElements(m.refs, s.refs, d.refs);
    visitElements(m.funs, s.funs, d.funs);
    return visitElement(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitField(AField m, Pair<AElement, AElement> arg) {
    return visitDeclaration(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitMethod(AMethod m, Pair<AElement, AElement> arg) {
    AMethod s = (AMethod) arg.fst;
    AMethod d = (AMethod) arg.snd;
    visitElements(m.bounds, s.bounds, d.bounds);
    visitElements(m.parameters, s.parameters, d.parameters);
    visitElements(m.throwsException, s.throwsException, d.throwsException);
    visitElements(m.parameters, s.parameters, d.parameters);
    visitBlock(m.body, elemPair(s.body, d.body));
    if (m.returnType != null) {
      m.returnType.accept(this, elemPair(s.returnType, d.returnType));
    }
    if (m.receiver != null) {
      m.receiver.accept(this, elemPair(s.receiver, d.receiver));
    }
    return visitDeclaration(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitTypeElement(ATypeElement m, Pair<AElement, AElement> arg) {
    ATypeElement s = (ATypeElement) arg.fst;
    ATypeElement d = (ATypeElement) arg.snd;
    visitElements(m.innerTypes, s.innerTypes, d.innerTypes);
    return visitElement(m, arg);
  }

  /**
   * Calculates difference between {@code m} and first component of
   * {@code arg}, storing result in second component.
   */
  @Override
  public Void visitTypeElementWithType(ATypeElementWithType m,
      Pair<AElement, AElement> arg) {
    return visitTypeElement(m, arg);
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  @Override
  public Void visitElement(AElement m, Pair<AElement, AElement> arg) {
    AElement s = arg.fst;
    AElement d = arg.snd;
    diff(m.tlAnnotationsHere, s.tlAnnotationsHere, d.tlAnnotationsHere);
    if (m.type != null) {
      AElement stype = s.type;
      AElement dtype = d.type;
      m.type.accept(this, elemPair(stype, dtype));
    }
    return null;
  }

  /**
   * Calculates difference between {@code m} and {@code s}, storing the
   * result in {@code d}.
   */
  private <K, V extends AElement>
  void visitElements(VivifyingMap<K, V> m, VivifyingMap<K, V> s,
      VivifyingMap<K, V> d) {
    if (m != null) {
      for (Map.Entry<K, V> e : m.entrySet()) {
        K key = e.getKey();
        V mval = e.getValue();
        V sval = s.get(key);
        if (sval == null) {
          d.put(key, mval);
        } else {
          mval.accept(this, elemPair(sval, d.vivify(key)));
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
  private static <K, V> void diff(Map<K, Set<V>> m, Map<K, Set<V>> s,
      Map<K, Set<V>> d) {
    if (m != null) {
      for (Map.Entry<K, Set<V>> me : m.entrySet()) {
        K key = me.getKey();
        Set<V> val = me.getValue();
        Set<V> mval = s.get(key);
        if (mval == null) {
          d.put(key, val);
        } else if (!mval.equals(val)) {
          try {
            @SuppressWarnings("unchecked")
            Set<V> diff = (Set<V>) mval.getClass().newInstance();
            diff(val, mval, diff);
            if (!diff.isEmpty()) {
              d.put(key, diff);
            }
          } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
          }
        }
      }
    }
  }

  /**
   * Convenience method for ensuring returned {@link Pair} is of the
   * most general type.
   */
  private Pair<AElement, AElement> elemPair(AElement stype, AElement dtype) {
    return Pair.of(stype, dtype);
  }
}
