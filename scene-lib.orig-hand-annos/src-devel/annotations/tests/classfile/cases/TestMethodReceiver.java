package annotations.tests.classfile.cases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestMethodReceiver {
  
  public void test() {
    System.out.println("test()");
  }
  
  private void test2() {
    System.out.println("test2()");
  }
  
  protected void test3() {
    System.out.println("test3()");
  }
  
  void test4() {
    System.out.println("test4()");
  }
}
