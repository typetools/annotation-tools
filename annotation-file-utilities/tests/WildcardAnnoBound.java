import java.util.List;

@interface Bla {}

public class WildcardAnnoBound<X extends List<? extends Object>> {
  WildcardAnnoBound(WildcardAnnoBound<X> n, X p) {
  }
}
