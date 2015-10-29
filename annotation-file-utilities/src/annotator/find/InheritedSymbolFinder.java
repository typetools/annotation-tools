package annotator.find;

import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import annotations.util.JVMNames;
import annotator.Source;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Filter;

/**
 * Provides static access to JVML descriptors of methods for which
 *  implementations are inherited in a given class.
 *
 * @author dbro
 */
public class InheritedSymbolFinder {
  private static Source source = null;

  // map of class (flat) names to sets of JVML method descriptors
  private static Map<String, Set<String>> cache =
      new TreeMap<String, Set<String>>();  // few classes expected

  private static Filter<Symbol> methodSymbolFilter =
      new Filter<Symbol>() {
        @Override
        public boolean accepts(Symbol t) {
          return t.getKind() == ElementKind.METHOD;
        }
      };

  private InheritedSymbolFinder() {}

  public static void setSource(Source source) {
    if (InheritedSymbolFinder.source != source) {
      InheritedSymbolFinder.source = source;
      cache.clear();
    }
  }

  /**
   * Finds whether a method matching a JVML descriptor has an inherited
   *  implementation.
   *
   * @param clazz type the class defines
   * @param methodDescriptor JVML descriptor for a method in the
   *         specified class
   * @return whether the method inherits its implementation from a
   *          superclass or interface
   */
  public static boolean isInheritedIn(Type type,
      String methodDescriptor) {
    return type.getKind() == TypeKind.DECLARED
        && isInheritedIn((Symbol.ClassSymbol) type.tsym, methodDescriptor);
  }

  /**
   * Finds whether a method matching a JVML descriptor has an inherited
   *  implementation in a given class.
   *
   * @param classSymbol symbol table entry for a class
   * @param methodDescriptor JVML descriptor for a method
   * @return whether a matching method exists in the class
   */
  public static boolean isInheritedIn(Symbol.ClassSymbol classSymbol,
      String methodDescriptor) {
    Set<String> inherited = getInherited(classSymbol);
    return inherited.contains(methodDescriptor);
  }

  /**
   * Finds inherited methods defined for a class (given by its symbol
   *  table entry).
   *
   * @param classSymbol symbol table entry for a class
   * @return JVML descriptors of methods in scope of class
   */
  private static Set<String>
  getInherited(final Symbol.ClassSymbol classSymbol) {
    if (source == null) { throw new IllegalStateException("null source"); }

    final Elements elements = source.getElementUtils();
    final Types types = source.getTypeUtils();
    String className = classSymbol.flatName().toString();

    if (elements == null) {
      throw new IllegalStateException("source.getElementUtils() returned null");
    }
    if (types == null) {
      throw new IllegalStateException("source.getTypeUtils() returned null");
    }
    if (cache.containsKey(className)) { return cache.get(className); }

    Set<String> names = new HashSet<String>();
    List<? extends Element> allMembers =
        elements.getAllMembers((TypeElement) classSymbol);
    Set<ExecutableElement> eelems =
        ElementFilter.methodsIn(new HashSet<Element>(allMembers));

    for (ExecutableElement eelem : eelems) {
      if (eelem instanceof Symbol.MethodSymbol) {
        Symbol.MethodSymbol msym = (Symbol.MethodSymbol) eelem;
        if (msym.owner != classSymbol) {
          names.add(JVMNames.getJVMMethodName(msym));
        }
      }
    }
    // cache unmodifiable view of result
    names = names.isEmpty() ? Collections.<String>emptySet()
        : Collections.<String>unmodifiableSet(names);
    cache.put(className, names);
    return names;
  }

  // Adds supertypes and implemented interfaces to working list.
  private static void addParents(final Symbol.ClassSymbol csym,
      Deque<Symbol.ClassSymbol> deque) {
    enqueueClassSymbol(csym.getSuperclass(), deque);
    for (Type interfaceType : csym.getInterfaces()) {
      enqueueClassSymbol(interfaceType, deque);
    }
  }

  // Adds type's symbol to working list iff type is a declared type.
  private static void enqueueClassSymbol(Type type,
      Deque<Symbol.ClassSymbol> deque) {
    if (type.getKind() == TypeKind.DECLARED) {
      Symbol.TypeSymbol tsym = ((Type.ClassType) type).tsym;
      Symbol.ClassSymbol csym = (Symbol.ClassSymbol) tsym;
      if (type.isInterface()) {
        deque.addLast(csym);
      } else {
        deque.addFirst(csym);
      }
    }
  }
}
