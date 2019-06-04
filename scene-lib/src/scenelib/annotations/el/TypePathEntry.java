package scenelib.annotations.el;

import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A TypePathEntry is a way to get from one node in a TypePath to another.
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
    assert step != TYPE_ARGUMENT || argument == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TypePathEntry that = (TypePathEntry) o;
    return step == that.step &&
        argument == that.argument;
  }

  @Override
  public int hashCode() {
    return Objects.hash(step, argument);
  }

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
      }
    }
    return TypePath.fromString(stringBuilder.toString());
  }

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
