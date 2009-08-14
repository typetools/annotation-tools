package annotator.specification;

import java.util.HashSet;
import java.util.Set;

import annotator.find.Criteria;
import annotator.find.Criterion;

/**
 * A CriterionList is a singly-linked list of Criterion meant to be treated
 * as a stack.  It is very useful for creating base criteria and passing
 * independent copies to different parts of a specification that creates
 * all the criterion.  A CriterionList is immutable, and so copies
 * created by the add() function can safely be passed anywhere.
 * It is supposed to be easier to manipulate than a Criteria.
 */
public class CriterionList {
  // This really is a simple data structure to facilitate creation
  //  of specifications.  TODO: make it a private class?
  private Criterion current;
  private CriterionList next;

  /**
   * Creates a new CriterionList with no criterion.
   */
  public CriterionList() {
    next = null;
    current = null;
  }

  /**
   * Creates a new CriterionList containing just the given Criterion.
   *
   * @param c the sole criterion the list contains at the moment
   */
  public CriterionList(Criterion c) {
    current = c;
    next = null;
  }

  private CriterionList(Criterion c, CriterionList n) {
    current = c;
    next = n;
  }

  /**
   * Adds the given criterion to the present list and returns a newly
   * allocated list containing the result.  Note that this will not lead to
   * a modification of the list this is called on.
   *
   * @param c the criterion to add
   * @return a new list containing the given criterion and the rest of the
   *  criterion already in this list
   */
  public CriterionList add(Criterion c) {
    return new CriterionList(c, this);
  }

  /**
   * Creates a Criteria object representing all the criterion in this list.
   *
   * @return a Criteria that contains all the criterion in this list
   */
  public Criteria criteria() {
    Criteria criteria = new Criteria();

    CriterionList c = this;
    Set<Criterion> criterion = new HashSet<Criterion>();
    while (c != null && c.current != null) {
      criteria.add(c.current);
      c = c.next;
    }

    return criteria;
  }

}
