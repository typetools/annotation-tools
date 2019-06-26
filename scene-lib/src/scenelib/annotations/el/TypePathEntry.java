package scenelib.annotations.el;

import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A TypePathEntry is a way to get from one node in a TypePath to another.
 * One can treat these as edges in a graph.
 * A list of TypePathEntry corresponds to a TypePath.
 */
public class TypePathEntry {
  /**
   * How to get from the previous node in a TypePath to this one.
   * One of TypePath.ARRAY_ELEMENT, TypePath.INNER_TYPE, TypePath.WILDCARD_BOUND, TypePath.TYPE_ARGUMENT.
   */
  public final int step;
  /**
   * If this represents a type argument (that is, step == TYPE_ARGUMENT, then the index for the type argument.
   * Otherwise, 0.
   */
  public final int argument;

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
   * @return the {@link TypePath} corresponding with the <code>integerList</code>.
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
        throw new IllegalArgumentException("Could not decode type path: " + integerList);
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
          throw new Error("This can't happen");
      }
    }
    return TypePath.fromString(stringBuilder.toString());
  }

  /**
   * Converts a type path represented by a list of integers to a list of {@link TypePathEntry} elements.
   * @param integerList the integer list in the form [step1, argument1, step2, argument2, ...] where step1 and argument1
   *                   are the step and argument of the first entry (or edge) of a type path.
   * @return the list of {@link TypePathEntry} elements corresponding with the <code>integerList</code>.
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
   * @param typePathEntryList the {@link TypePathEntry} list corresponding to the location of some type annotation.
   * @return the {@link TypePath} corresponding with the <code>typePathEntryList</code>.
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
   * @param typePath the {@link TypePath} corresponding to the location of some type annotation.
   * @return the list of {@link TypePathEntry} elements corresponding with the <code>typePath</code>.
   */
  public static List<TypePathEntry> typePathToList(TypePath typePath) {
    List<TypePathEntry> typePathEntryList = new ArrayList<>(typePath.getLength());
    if (typePath == null) {
      return null;
    }
    for (int step = 0; step < typePath.getLength(); step++) {
      typePathEntryList.add(new TypePathEntry(typePath.getStep(step), typePath.getStepArgument(step)));
    }
    return typePathEntryList;
  }
}
