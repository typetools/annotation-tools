package scenelib.annotations.util;

/**
 * Stack that creates new stacks rather than modify data in place.
 *
 * @author dbro
 * @param <E> type of stack elements
 */
public interface ImmutableStack<E> {
  public boolean isEmpty();
  public E peek();
  public ImmutableStack<E> pop();
  public ImmutableStack<E> push(E elem);
  public int size();
}
