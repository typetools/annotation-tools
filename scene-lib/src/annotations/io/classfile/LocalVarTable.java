package annotations.io.classfile;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;

import annotations.el.LocalLocation;

public class LocalVarTable {
  private final List<Entry> entries;

  LocalVarTable() {
    entries = new ArrayList<Entry>();
  }

  public Entry get(int start, int end, int index) {
    for (Entry e : entries) {
      if (e.start.getOffset() == start
          && e.end.getOffset() == end
          && e.index == index) {
        return e;
      }
    }
    return null;
  }

  public Entry get(LocalLocation localLocation) {
    int start = localLocation.scopeStart;
    int end = start + localLocation.scopeLength;
    return get(start, end, localLocation.index);
  }

  public Entry get(String name, String desc) {
    String key = name+':'+desc;
    for (Entry e : entries) {
      if (key.equals(e.key)) {
        return e;
      }
    }
    return null;
  }

  public void put(String name, String desc, String signature,
      Label start, Label end, int index) {
    entries.add(new Entry(name+':'+desc, start, end, index));
  }

  static class Entry {
    final Label start;
    final Label end;
    final int index;
    final String key;

    Entry(String desc, Label start, Label end, int index) {
      this.key = desc;
      this.start = start;
      this.end = end;
      this.index = index;
    }

    public boolean equals(Entry e) {
      return e != null && key == e.key && index == e.index
          && start.equals(e.start) && end.equals(e.end);
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Entry && equals((Entry) obj);
    }

    @Override
    public int hashCode() {
      return makeHash(index, start, end, key);
    }

    private int makeHash(int i, Object... objects) {
      int h = i;
      for (Object o : objects) {
        if (o != null) {
          h &= o.hashCode();
        }
      }
      return h;
    }

    @Override
    public String toString() {
      return key + ":" + index
          + "," + start.getOffset()
          + "," + end.getOffset();
    }
  }

  public Iterable<Entry> getEntries() {
    return entries;
  }

  @Override
  public String toString() {
    return entries.toString();
  }
}
