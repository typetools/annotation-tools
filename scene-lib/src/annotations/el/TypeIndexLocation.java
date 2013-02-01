package annotations.el;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

import annotations.util.Hasher;

public class TypeIndexLocation {
  public final int typeIndex;

  public TypeIndexLocation(int typeIndex) {
      this.typeIndex = typeIndex;
  }

  public boolean equals(TypeIndexLocation l) {
      return typeIndex == l.typeIndex;
  }

  @Override
  public boolean equals(/*@ReadOnly*/ Object o) {
      return o instanceof TypeIndexLocation
              && equals((TypeIndexLocation) o);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode(/*>>> @ReadOnly TypeIndexLocation this*/) {
      Hasher h = new Hasher();
      h.mash(typeIndex);
      return h.hash;
  }

  @Override
  public String toString() {
      return "TypeIndexLocation(" + typeIndex + ")";
  }

}
