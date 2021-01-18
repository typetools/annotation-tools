package scenelib.annotations.el;

import org.checkerframework.checker.interning.qual.InternedDistinct;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A TypePathEntry is a way to get from one node in a {@link TypePath} to another.
 * One can treat these as edges in a graph.
 *
 * ASM has a data structure {@link TypePath}.  {@code List<TypePathEntry>}
 * corresponds to a {@code TypePath}.  That is, each TypePathEntry corresponds
 * to a step in an ASM TypePath.
 *
 * {@code List<TypePathEntry>} also corresponds to the javac class
 * {@code com.sun.tools.javac.code.TypeAnnotationPosition}.
 *
 * {@code TypePathEntry} is immutable.
 */
public class TypePathEntry {
  /**
   * The kind of TypePathEntry; that is, how to get from the previous node in a TypePath to this one.
   * One of TypePath.ARRAY_ELEMENT, TypePath.INNER_TYPE, TypePath.WILDCARD_BOUND, TypePath.TYPE_ARGUMENT.
   *
   * This corresponds to javac class {@code com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry}.
   */
  public final int step;
  /**
   * If this represents a type argument (that is, step == TYPE_ARGUMENT, then the index for the type argument.
   * Otherwise, 0.
   */
  public final int argument;

  public static final @InternedDistinct TypePathEntry ARRAY_ELEMENT = new TypePathEntry(TypePath.ARRAY_ELEMENT, 0);
  public static final @InternedDistinct TypePathEntry INNER_TYPE = new TypePathEntry(TypePath.INNER_TYPE, 0);
  public static final @InternedDistinct TypePathEntry WILDCARD_BOUND = new TypePathEntry(TypePath.WILDCARD_BOUND, 0);

  /**
   * Construct a new TypePathEntry.
   *
   * @param step the type of the TypePathEntry
   * @param argument index of the type argument or 0
   */
  public TypePathEntry(int step, int argument) {
    this.step = step;
    this.argument = argument;

    assert step == TypePath.ARRAY_ELEMENT || step == TypePath.INNER_TYPE
        || step == TypePath.WILDCARD_BOUND || step == TypePath.TYPE_ARGUMENT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TypePathEntry that = (TypePathEntry) o;
    return step == that.step && argument == that.argument;
  }

  @Override
  public int hashCode() {
    return Objects.hash(step, argument);
  }

  /**
   * Converts a type path represented by a list of integers to a {@link TypePath}.
   * @param integerList the integer list in the form [step1, argument1, step2, argument2, ...] where step1 and argument1
   *                   are the step and argument of the first entry (or edge) of a type path.
   *    Each step is a {@link TypePath} constant; see {@link #step}.
   * @return the {@link TypePath} corresponding to the <code>integerList</code>.
   */
  public static TypePath getTypePathFromBinary(List<Integer> integerList) {
    if (integerList == null) {
      return null;
    }
    StringBuilder stringBuilder = new StringBuilder();
    Iterator<Integer> iterator = integerList.iterator();
    while (iterator.hasNext()) {
      int step = iterator.next();
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Odd number of elements: " + integerList);
      }
      int argument = iterator.next();
      switch (step) {
        case TypePath.ARRAY_ELEMENT:
          stringBuilder.append('[');
          break;
        case TypePath.INNER_TYPE:
          stringBuilder.append('.');
          break;
        case TypePath.WILDCARD_BOUND:
          stringBuilder.append('*');
          break;
        case TypePath.TYPE_ARGUMENT:
          stringBuilder.append(argument).append(';');
          break;
        default:
          throw new Error("Bad step " + step);
      }
    }
    return TypePath.fromString(stringBuilder.toString());
  }

  /**
   * Converts a type path represented by a list of integers to a list of {@link TypePathEntry} elements.
   * @param integerList the integer list in the form [step1, argument1, step2, argument2, ...] where step1 and argument1
   *                   are the step and argument of the first entry (or edge) of a type path.
   *    Each step is a {@link TypePath} constant; see {@link #step}.
   * @return the list of {@link TypePathEntry} elements corresponding to the <code>integerList</code>,
   *  or null if the argument is null
   */
  public static List<TypePathEntry> getTypePathEntryListFromBinary(List<Integer> integerList) {
    if (integerList == null) {
      return null;
    }
    List<TypePathEntry> typePathEntryList = new ArrayList<>();
    Iterator<Integer> iterator = integerList.iterator();
    while (iterator.hasNext()) {
      int step = iterator.next();
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Could not decode type path: " + integerList);
      }
      int argument = iterator.next();
      typePathEntryList.add(new TypePathEntry(step, argument));
    }
    return typePathEntryList;
  }

  /**
   * Converts a type path represented by a list of integers to a {@link TypePath}.
   * @param typePathEntryList the {@link TypePathEntry} list corresponding to the location of some type annotation
   * @return the {@link TypePath} corresponding to the <code>typePathEntryList</code>.
   */
  public static TypePath listToTypePath(List<TypePathEntry> typePathEntryList) {
    if (typePathEntryList == null || typePathEntryList.isEmpty()) {
      return null;
    }
    StringBuilder stringBuilder = new StringBuilder();
    for (TypePathEntry typePathEntry : typePathEntryList) {
      switch (typePathEntry.step) {
        case TypePath.ARRAY_ELEMENT:
          stringBuilder.append('[');
          break;
        case TypePath.INNER_TYPE:
          stringBuilder.append('.');
          break;
        case TypePath.WILDCARD_BOUND:
          stringBuilder.append('*');
          break;
        case TypePath.TYPE_ARGUMENT:
          stringBuilder.append(typePathEntry.argument).append(';');
          break;
        default:
          throw new Error("This can't happen");
      }
    }
    return TypePath.fromString(stringBuilder.toString());
  }

  /**
   * Converts a {@link TypePath} to a list of {@link TypePathEntry} elements.
   * @param typePath the {@link TypePath} corresponding to the location of some type annotation
   * @return the list of {@link TypePathEntry} elements corresponding to the <code>typePath</code>,
   *   or null if the argument is null
   */
  public static List<TypePathEntry> typePathToList(TypePath typePath) {
    if (typePath == null) {
      return null;
    }
    List<TypePathEntry> typePathEntryList = new ArrayList<>(typePath.getLength());
    for (int index = 0; index < typePath.getLength(); index++) {
      typePathEntryList.add(new TypePathEntry(typePath.getStep(index), typePath.getStepArgument(index)));
    }
    return typePathEntryList;
  }
}
