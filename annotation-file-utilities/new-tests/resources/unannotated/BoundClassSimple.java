package resources.unannotated;

import java.util.Date;

public class BoundClassSimple<T extends Date> {
  T field = null;
  Date misleadingField = new Date();
}
