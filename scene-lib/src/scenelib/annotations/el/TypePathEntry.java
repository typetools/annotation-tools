package scenelib.annotations.el;

import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypePathEntry {
  public final int step;
  public final int argument;

  public TypePathEntry(int step, int argument) {
    this.step = step;
    this.argument = argument;
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
}