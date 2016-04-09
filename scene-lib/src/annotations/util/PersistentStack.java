package annotations.util;

/**
 * Stack that creates new stacks rather than modify data in place.
 *
 * @author dbro
 * @param <E> type of stack elements
 */
public interface PersistentStack<E> {
  public boolean isEmpty();
  public E peek();
  public PersistentStack<E> pop();
  public PersistentStack<E> push(E elem);
  public int size();
}
