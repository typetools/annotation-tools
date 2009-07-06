package annotator.tests;

import java.io.Serializable;

import com.sun.tools.javac.util.List;

public class BoundClassMultiple<
  T extends Date, 
  U extends List & Serializable, 
  V extends Serializable> {
}
