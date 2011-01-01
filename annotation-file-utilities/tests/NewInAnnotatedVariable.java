@interface Nullable {}
@interface NonNull {}

public class NewInAnnotatedVariable {
  Number b1 = new Integer(0);
  @NonNull Object b2 = new /*@Nullable*/ Double(1);
  @NonNull Runnable b3 = new /*@NonNull*/ Thread();
  ThreadLocal[] b4 = new InheritableThreadLocal[3];
}

