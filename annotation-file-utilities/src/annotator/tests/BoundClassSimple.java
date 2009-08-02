package annotator.tests;

public class BoundClassSimple<T extends Date> {
  T field = null;
  Date misleadingField = new Date();
}
