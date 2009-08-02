package annotator.tests;

import java.util.List;

public class GenericCell {
  private List<IntCell> internalList;
  
  public GenericCell(List<IntCell> list) {
    internalList = list;
  }
  
  public List<IntCell> getList() {
    return internalList;
  }
}
