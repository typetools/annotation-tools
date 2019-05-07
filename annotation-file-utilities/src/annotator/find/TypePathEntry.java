package annotator.find;

public class TypePathEntry {
  public final int step;
  public final int argument;

  public TypePathEntry(int step, int argument) {
    this.step = step;
    this.argument = argument;
  }
}