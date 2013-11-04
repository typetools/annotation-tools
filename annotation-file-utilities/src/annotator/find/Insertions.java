package annotator.find;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author dbro
 *
 */
public class Insertions implements Iterable<Insertion> {
  private static final Set<Insertion> EMPTY_SET =
      Collections.<Insertion>emptySet();
  private Map<String, Set<Insertion>> store;
  private int size;

  public Insertions() {
    store = new HashMap<String, Set<Insertion>>();
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
    if (!store.containsKey(key)) {
      store.put(key, new HashSet<Insertion>());
    }
    store.get(key).add(ins);
    ++size;
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
}
