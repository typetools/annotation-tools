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
 * A TypePathEntry corresponds
 * to a step in an ASM {@link TypePath}.
 *
 * {@code List<TypePathEntry>} corresponds to an ASM {@link TypePath}.
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
   * If this represents a type argument (that is, step == TYPE_ARGUMENT), then the index for the type argument.
   * Otherwise, 0.
   */
  public final int argument;

  /** The canonical ARRAY_ELEMENT TypePathEntry for building TypePaths. */
  public static final @InternedDistinct TypePathEntry ARRAY_ELEMENT = new TypePathEntry(TypePath.ARRAY_ELEMENT, 0);
  /** The canonical INNER_TYPE TypePathEntry for building TypePaths. */
  public static final @InternedDistinct TypePathEntry INNER_TYPE = new TypePathEntry(TypePath.INNER_TYPE, 0);
  /** The canonical WILDCARD_BOUND TypePathEntry for building TypePaths. */
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
   * Converts a TypePathEntry to a String.  The TypePathEntry is passed in as its
   * component parts: step and argument.
   *
   * @param step the kind of TypePathEntry
   * @param argument a type index if the step == TYPE_ARGUMENT, otherwise ignored
   * @return the String reresentaion of the TypePathEntry
   */
  public static String stepToString(int step, int argument) {
    switch (step) {
      case TypePath.ARRAY_ELEMENT:
        return "[";
      case TypePath.INNER_TYPE:
        return ".";
      case TypePath.WILDCARD_BOUND:
        return "*";
      case TypePath.TYPE_ARGUMENT:
        return String.valueOf(argument) + ";";
      default:
        throw new Error("Bad step " + step);
    }
  }

  /**
   * Converts a type path represented by a list of integers to a {@link TypePath}.
   * @param integerList the integer list in the form [step1, argument1, step2, argument2, ...] where step1 and argument1
   *                   are the step and argument of the first entry (or edge) of a type path.
   *    Each step is a {@link TypePath} constant; see {@link #step}.
   * @return the {@link TypePath} corresponding to {@code integerList},
   *   or null if the argument is null
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
      stringBuilder.append(stepToString(step, argument));
    }
    return TypePath.fromString(stringBuilder.toString());
  }

  /**
   * Converts a type path represented by a list of integers to a list of {@link TypePathEntry} elements.
   * @param integerList the integer list in the form [step1, argument1, step2, argument2, ...] where step1 and argument1
   *                   are the step and argument of the first entry (or edge) of a type path.
   *    Each step is a {@link TypePath} constant; see {@link #step}.
   * @return the list of {@link TypePathEntry} elements corresponding to {@code integerList},
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
        throw new IllegalArgumentException("Odd number of elements: " + integerList);
      }
      int argument = iterator.next();
      typePathEntryList.add(new TypePathEntry(step, argument));
    }
    return typePathEntryList;
  }

  /**
   * Converts a type path represented by a list of integers to a {@link TypePath}.
   * @param typePathEntryList the {@link TypePathEntry} list corresponding to the location of some type annotation
   * @return the {@link TypePath} corresponding to {@code typePathEntryList},
   *   or null if the argument is null or empty
   */
  public static TypePath listToTypePath(List<TypePathEntry> typePathEntryList) {
    if (typePathEntryList == null || typePathEntryList.isEmpty()) {
      return null;
    }
    StringBuilder stringBuilder = new StringBuilder();
    for (TypePathEntry typePathEntry : typePathEntryList) {
      stringBuilder.append(stepToString(typePathEntry.step, typePathEntry.argument));
    }
    return TypePath.fromString(stringBuilder.toString());
  }

  /**
   * Converts a {@link TypePath} to a list of {@link TypePathEntry} elements.
   * @param typePath the {@link TypePath} corresponding to the location of some type annotation
   * @return the list of {@link TypePathEntry} elements corresponding to {@code typePath},
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
