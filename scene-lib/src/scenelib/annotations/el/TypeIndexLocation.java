package scenelib.annotations.el;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

import scenelib.annotations.util.Hasher;

public class TypeIndexLocation {
  public final int typeIndex;

  public TypeIndexLocation(int typeIndex) {
      this.typeIndex = typeIndex;
  }

  public boolean equals(TypeIndexLocation l) {
      return typeIndex == l.typeIndex;
  }

  @Override
  public boolean equals(Object o) {
      return o instanceof TypeIndexLocation
              && equals((TypeIndexLocation) o);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
      Hasher h = new Hasher();
      h.mash(typeIndex);
      return h.hash;
  }

  @Override
  public String toString() {
      return "TypeIndexLocation(" + typeIndex + ")";
  }

}
