package annotator.find;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.element.ElementKind;

import annotations.util.JVMNames;
import annotator.Source;

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
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

  private InheritedSymbolFinder() {}

  public static void setSource(Source source) {
    if (InheritedSymbolFinder.source != source) {
      InheritedSymbolFinder.source = source;
      cache.clear();
    }
  }

  /**
   * Finds whether a method matching a JVML descriptor has an inherited
   *  implementation in a given class.
   *
   * @param csym symbol table entry for a class
   * @param methodName JVML descriptor for a method
   * @return whether a matching method exists in the class
   */
  public static boolean isInheritedIn(Symbol.ClassSymbol csym,
      String methodName) {
    Set<String> inherited = getInherited(csym);
    return inherited.contains(methodName);
  }

  /**
   * Finds inherited methods defined for a given class.
   *
   * @param csym symbol table entry for a class
   * @return JVML descriptors of methods in scope of class
   */
  public static Set<String> getInherited(final Symbol.ClassSymbol csym) {
    String className = csym.flatName().toString();
    if (cache.containsKey(className)) { return cache.get(className); }
    if (source == null) { throw new IllegalStateException("null source"); }

    final Types types = source.getTypes();
    if (types == null) {
      throw new IllegalStateException("source.getTypes() returned null");
    }

    Filter<Symbol> filter = new Filter<Symbol>() {
      @Override
      public boolean accepts(final Symbol t) {
        return t.getKind() == ElementKind.METHOD
            && t.isInheritedIn(csym, types);
      }
    };
    Set<String> ss = new HashSet<String>();
    Deque<Type.ClassType> td = new ArrayDeque<Type.ClassType>();
    addParents((Type.ClassType) csym.type, td);
    //Deque<Symbol.ClassSymbol> syms = new ArrayDeque<Symbol.ClassSymbol>();
    //addParents(csym, syms);

    while (!td.isEmpty()) {
      Type.ClassType type = td.removeFirst();
      Scope scope = type.tsym.members();
      if (scope == null) { continue; }

      for (Symbol sym : scope.getElements(filter)) {
        if (sym.owner == csym) { continue; }  // reject if defined here
        Symbol.MethodSymbol msym = (Symbol.MethodSymbol) sym;
        StringBuilder sb =
            new StringBuilder(msym.getSimpleName()).append('(');
        for (Symbol.VarSymbol param : msym.getParameters()) {
          sb = sb.append(JVMNames.typeToJvmlString(param.type));
        }
        sb = sb.append(')').append(
            JVMNames.typeToJvmlString(msym.getReturnType()));
        ss.add(sb.toString());
      }

      addParents(type, td);
    }

    Set<String> names = ss.isEmpty() ? Collections.<String>emptySet()
        : Collections.<String>unmodifiableSet(ss);
    cache.put(className, names);
    return names;
  }

  private static void addParents(final Type.ClassType ct,
      Deque<Type.ClassType> td) {
    Type st = ct.supertype_field;
    List<Type> its = ct.all_interfaces_field;
    if (st != null && !st.isErroneous() && st.hasTag(TypeTag.CLASS)) {
      td.addFirst((Type.ClassType) st);
    }
    if (its != null) {
      for (Type it : ct.all_interfaces_field) {
        if (!it.isErroneous() && it.hasTag(TypeTag.CLASS)) {
          td.addLast((Type.ClassType) it);
        }
      }
    }
  }
}
