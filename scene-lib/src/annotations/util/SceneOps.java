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
 * Algebraic operations on scenes.
 *
 * @author dbro
 */
public class SceneOps {
  private SceneOps() {}

  /**
   * Run an operation on a subcommand-specific number of JAIFs.
   * Currently the only available subcommand is "diff", which must be
   * the first of three arguments, followed in order by the "minuend"
   * and the "subtrahend" (see {@link #diff(AScene, AScene)}).  If
   * successful, the diff subcommand writes the scene it calculates to
   * {@link System#out}.
   *
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 3 || !"diff".equals(args[0])) {
      System.err.println(
          "usage: java annotations.util.SceneOps diff first.jaif second.jaif");
      System.exit(1);
    }

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
   * @param s1 the "minuend"
   * @param s2 the "subtrahend"
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

  // TODO: integrate into scene-lib test suite
  public static void testDiffEmpties() {
    assert new AScene().equals(diff(new AScene(), new AScene()));
  }
  public static void testDiffSame() throws IOException {
    AScene scene1 = new AScene();
    AScene scene2 = new AScene();
    String dirname =
        "test/annotations/tests/classfile/cases";
    String[] cases = { "ClassEmpty", "ClassNonEmpty", "FieldGeneric",
        "FieldSimple", "LocalVariableGenericArray", "MethodReceiver",
        "MethodReturnTypeGenericArray", "ObjectCreationGenericArray",
        "ObjectCreation", "TypecastGenericArray", "Typecast" };
    for (int i = 0; i < cases.length; i++) {
      String filename = dirname+"/Test"+cases[i]+".jaif";
      IndexFileParser.parseFile(filename, scene1);
      IndexFileParser.parseFile(filename, scene2);
      assert new AScene().equals(diff(scene1, scene1));
      assert new AScene().equals(diff(scene1, scene2));
    }
  }
}

/**
 * Visitor for calculating "set difference" of scenes.
 * Visitor methods fill in a scene instead of returning one because an
 * {@link AElement} can be created only inside an {@link AScene}. 
 *
 * @author dbro
 */
class DiffVisitor
implements ElementVisitor<Void, Pair<AElement, AElement>> {
  /**
   * Compute the difference of two scenes, that is, a scene containing
   * all and only those insertion specifications that exist in the first
   * but not in the second.
   *
   * @param s1 the "minuend"
   * @param s2 the "subtrahend"
   * @return s1 - s2 ("set difference")
   */
  public static AScene diff(AScene s1, AScene s2) {
    AScene difference = new AScene();
    new DiffVisitor().visitScene(s1, s2, difference);
    difference.prune();
    return difference;
  }

  /**
   * Adds all annotations that are in {@code minuend} but not in
   * {@code subtrahend} to {@code difference}.
   */
  public void visitScene(AScene minuend, AScene subtrahend,
      AScene difference) {
    visitElements(minuend.packages, subtrahend.packages,
        difference.packages);
    diff(minuend.imports, subtrahend.imports, difference.imports);
    visitElements(minuend.classes, subtrahend.classes,
        difference.classes);
  }

  // Never used, as annotations and definitions don't get duplicated.
  @Override
  public Void visitAnnotationDef(AnnotationDef minuend,
      Pair<AElement, AElement> arg) {
    throw new IllegalStateException(
        "BUG: DiffVisitor.visitAnnotationDef invoked");
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitBlock(ABlock minuend, Pair<AElement, AElement> arg) {
    ABlock subtrahend = (ABlock) arg.fst;
    ABlock difference = (ABlock) arg.snd;
    visitElements(minuend.locals, subtrahend.locals, difference.locals);
    return visitExpression(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitClass(AClass minuend, Pair<AElement, AElement> arg) {
    AClass subtrahend = (AClass) arg.fst;
    AClass difference = (AClass) arg.snd;
    visitElements(minuend.bounds, subtrahend.bounds, difference.bounds);
    visitElements(minuend.extendsImplements,
        subtrahend.extendsImplements, difference.extendsImplements);
    visitElements(minuend.methods, subtrahend.methods,
        difference.methods);
    visitElements(minuend.staticInits, subtrahend.staticInits,
        difference.staticInits);
    visitElements(minuend.instanceInits, subtrahend.instanceInits,
        difference.instanceInits);
    visitElements(minuend.fields, subtrahend.fields, difference.fields);
    visitElements(minuend.fieldInits, subtrahend.fieldInits,
        difference.fieldInits);
    return visitDeclaration(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitDeclaration(ADeclaration minuend,
      Pair<AElement, AElement> arg) {
    ADeclaration subtrahend = (ADeclaration) arg.fst;
    ADeclaration difference = (ADeclaration) arg.snd;
    visitElements(minuend.insertAnnotations,
        subtrahend.insertAnnotations, difference.insertAnnotations);
    visitElements(minuend.insertTypecasts, subtrahend.insertTypecasts,
        difference.insertTypecasts);
    return visitElement(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitExpression(AExpression minuend,
      Pair<AElement, AElement> arg) {
    AExpression subtrahend = (AExpression) arg.fst;
    AExpression difference = (AExpression) arg.snd;
    visitElements(minuend.typecasts, subtrahend.typecasts,
        difference.typecasts);
    visitElements(minuend.instanceofs, subtrahend.instanceofs,
        difference.instanceofs);
    visitElements(minuend.news, subtrahend.news, difference.news);
    visitElements(minuend.calls, subtrahend.calls, difference.calls);
    visitElements(minuend.refs, subtrahend.refs, difference.refs);
    visitElements(minuend.funs, subtrahend.funs, difference.funs);
    return visitElement(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitField(AField minuend, Pair<AElement, AElement> arg) {
    return visitDeclaration(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitMethod(AMethod minuend,
      Pair<AElement, AElement> arg) {
    AMethod subtrahend = (AMethod) arg.fst;
    AMethod difference = (AMethod) arg.snd;
    visitElements(minuend.bounds, subtrahend.bounds, difference.bounds);
    visitElements(minuend.parameters, subtrahend.parameters,
        difference.parameters);
    visitElements(minuend.throwsException, subtrahend.throwsException,
        difference.throwsException);
    visitElements(minuend.parameters, subtrahend.parameters,
        difference.parameters);
    visitBlock(minuend.body,
        elemPair(subtrahend.body, difference.body));
    if (minuend.returnType != null) {
      minuend.returnType.accept(this,
          elemPair(subtrahend.returnType, difference.returnType));
    }
    if (minuend.receiver != null) {
      minuend.receiver.accept(this,
          elemPair(subtrahend.receiver, difference.receiver));
    }
    return visitDeclaration(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitTypeElement(ATypeElement minuend,
      Pair<AElement, AElement> arg) {
    ATypeElement subtrahend = (ATypeElement) arg.fst;
    ATypeElement difference = (ATypeElement) arg.snd;
    visitElements(minuend.innerTypes, subtrahend.innerTypes,
        difference.innerTypes);
    return visitElement(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitTypeElementWithType(ATypeElementWithType minuend,
      Pair<AElement, AElement> arg) {
    return visitTypeElement(minuend, arg);
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  @Override
  public Void visitElement(AElement minuend,
      Pair<AElement, AElement> arg) {
    AElement subtrahend = arg.fst;
    AElement difference = arg.snd;
    diff(minuend.tlAnnotationsHere, subtrahend.tlAnnotationsHere,
        difference.tlAnnotationsHere);
    if (minuend.type != null) {
      AElement stype = subtrahend.type;
      AElement dtype = difference.type;
      minuend.type.accept(this, elemPair(stype, dtype));
    }
    return null;
  }

  /**
   * Calculates difference between {@code minuend} and first component
   * of {@code arg}, adding results to second component of {@code arg}.
   */
  private <K, V extends AElement>
  void visitElements(VivifyingMap<K, V> minuend,
      VivifyingMap<K, V> subtrahend, VivifyingMap<K, V> difference) {
    if (minuend != null) {
      for (Map.Entry<K, V> e : minuend.entrySet()) {
        K key = e.getKey();
        V mval = e.getValue();
        V sval = subtrahend.get(key);
        if (sval == null) {
          difference.put(key, mval);
        } else {
          mval.accept(this, elemPair(sval, difference.vivify(key)));
        }
      }
    }
  }

  /**
   * Calculates difference between {@code minuend} and
   * {@code subtrahend}, storing the result in {@code difference}.
   */
  private static <T> void diff(Set<T> minuend, Set<T> subtrahend,
      Set<T> difference) {
    if (minuend != null) {
      for (T t : minuend) {
        if (!subtrahend.contains(t)) {
          difference.add(t);
        }
      }
    }
  }

  /**
   * Calculates difference between {@code minuend} and
   * {@code subtrahend}, adding the results to {@code difference}.
   */
  private static <K, V> void diff(Map<K, Set<V>> minuend,
      Map<K, Set<V>> subtrahend, Map<K, Set<V>> difference) {
    if (minuend != null) {
      for (K key : minuend.keySet()) {
        Set<V> mval = minuend.get(key);
        Set<V> sval = subtrahend.get(key);
        if (sval == null) {
          difference.put(key, mval);
        } else if (!sval.equals(mval)) {
          try {
            @SuppressWarnings("unchecked")
            Set<V> set = (Set<V>) sval.getClass().newInstance();
            diff(mval, sval, set);
            if (!set.isEmpty()) {
              difference.put(key, set);
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
  private Pair<AElement, AElement> elemPair(AElement stype,
      AElement dtype) {
    return Pair.of(stype, dtype);
  }
}
