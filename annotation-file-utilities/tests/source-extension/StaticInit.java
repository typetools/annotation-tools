package annotator.tests;

public class StaticInit {
  static void blabla() {}

  static {
    @SuppressWarnings({"deprecation", "removal"})
    Object o = new String("hello");
    if (o instanceof Integer) {
      Object o2 = new Object();
    }
  }

  void m() {
    if (true) {
    } else {
    }
  }

  static {
    StaticInit si = new StaticInit();
  }
}
