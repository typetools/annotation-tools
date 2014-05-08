package annotator.find;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author dbro
 *
 * The motivation for this class was to avoid wasting time going through an entire scene
 * (which could involve many different classes) for every class. This makes an immense
 * difference when a JAIF covers every class in a large codebase such as Hadoop.
 *
 */
public class Insertions implements Iterable<Insertion> {
  private static final Set<Insertion> EMPTY_SET =
      Collections.<Insertion>emptySet();
  private Map<String, Set<Insertion>> store; // Map of class name to insertions
  
  private int size;

  public Insertions() {
    store = new HashMap<String, Set<Insertion>>();
    size = 0;
  }

  public Set<Insertion> forClass(String cls) {
    return store.containsKey(cls) ? store.get(cls) : EMPTY_SET; 
  }

  public void add(Insertion ins) {
    InClassCriterion icc = ins.getCriteria().getInClass();
    String key = "";
    if (icc != null) {
      int i = icc.className.lastIndexOf('.');
      key = i < 0 ? icc.className : icc.className.substring(i + 1);
      i = key.indexOf('$');
      if (i > 0) { key = key.substring(0, i); }
    }

    Set<Insertion> val = store.get(key);
    if (val == null) {
      val = new HashSet<Insertion>();
      store.put(key, val);
    }
    size -= val.size();
    val.add(ins);
    size += val.size();;
  }

  public void addAll(Collection<? extends Insertion> c) {
    for (Insertion ins : c) {
      add(ins);
    }
  }

  public int size() {
    return size;
  }

  @Override
  public Iterator<Insertion> iterator() {
    return new Iterator<Insertion>() {
      Iterator<Set<Insertion>> siter = store.values().iterator();
      Iterator<Insertion> iiter = EMPTY_SET.iterator();

      @Override
      public boolean hasNext() {
        if (iiter.hasNext()) { return true; }
        while (siter.hasNext()) {
          iiter = siter.next().iterator();
          if (iiter.hasNext()) {
            return true;
          }
        }
        return false;
      }

      @Override
      public Insertion next() {
        if (hasNext()) { return iiter.next(); }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public List<Insertion> toList() {
    List<Insertion> list = new ArrayList<Insertion>(size);
    for (Set<Insertion> insertions : store.values()) {
      list.addAll(insertions);
    }
    return null;
  }
}
