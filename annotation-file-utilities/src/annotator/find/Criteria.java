package annotator.find;

import annotator.Main;

import java.util.*;

import annotations.el.BoundLocation;
import annotations.el.InnerTypeLocation;
import annotations.el.LocalLocation;
import annotations.el.RelativeLocation;
import annotations.el.TypeIndexLocation;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Represents a set of Criterion objects for locating a program element in
 * a source tree.
 * <p>
 *
 * This class also contains static factory methods for creating a {@code
 * Criterion}.
 */
public final class Criteria {

  // Not final, to avoid tests against it being optimized away.
  public static boolean debug = false;

  /** The set of criterion objects. */
  private final Set<Criterion> criteria;

  /**
   * Creates a new {@code Criteria} without any {@code Criterion}.
   */
  public Criteria() {
    this.criteria = new LinkedHashSet<Criterion>();
  }

  /**
   * Add a {@code Criterion} to this {@code Criteria}.
   *
   * @param c the criterion to add
   */
  public void add(Criterion c) {
    criteria.add(c);
  }

  /**
   * Determines whether or not the program element at the leaf of the
   * specified path is satisfied by these criteria.
   *
   * @param path the tree path to check against
   * @param leaf the tree at the leaf of the path; only relevant when the path
   *        is null, in which case the leaf is a CompilationUnitTree
   * @return true if all of these criteria are satisfied by the given path,
   * false otherwise
   */
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    assert path == null || path.getLeaf() == leaf;
    for (Criterion c : criteria) {
      if (! c.isSatisfiedBy(path, leaf)) {
    	if (debug) {
          System.out.printf("UNsatisfied criterion:%n    %s%n    %s%n", c, Main.pathToString(path));
    	}
        return false;
      } else if (debug) {
    	System.out.printf("satisfied criterion:%n    %s%n    %s%n", c, Main.pathToString(path));
      }
    }
    return true;
  }

  /**
   * Determines whether or not the program element at the leaf of the
   * specified path is satisfied by these criteria.
   *
   * @param path the tree path to check against
   * @return true if all of these criteria are satisfied by the given path,
   * false otherwise
   */
  public boolean isSatisfiedBy(TreePath path) {
    for (Criterion c : criteria) {
      if (! c.isSatisfiedBy(path)) {
        if (debug) {
          System.out.println("UNsatisfied criterion: " + c);
        }
        return false;
      } else if (debug) {
    	System.out.println("satisfied criterion: " + c);
      }
    }
    return true;
  }

  /**
   * Determines whether this is the criteria on a receiver.
   *
   * @return true iff this is the criteria on a receiver
   */
  public boolean isOnReceiver() {
    for (Criterion c : criteria) {
      if (c.getKind() == Criterion.Kind.RECEIVER) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether this is the criteria on a return type.
   *
   * @return true iff this is the criteria on a return type
   */
  public boolean isOnReturnType() {
    for (Criterion c : criteria) {
      if (c.getKind() == Criterion.Kind.RETURN_TYPE) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether this is the criteria on a local variable.
   *
   * @return true iff this is the criteria on a local variable
   */
  public boolean isOnLocalVariable() {
    for (Criterion c : criteria) {
      if (c.getKind() == Criterion.Kind.LOCAL_VARIABLE) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true if this Criteria is on the given method.
   */
  public boolean isOnMethod(String methodname) {
    for (Criterion c : criteria) {
      if (c.getKind() == Criterion.Kind.IN_METHOD) {
        if (((InMethodCriterion) c).name.equals(methodname)) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * @return a GenericArrayLocationCriterion if this has one, else null
   */
  public GenericArrayLocationCriterion getGenericArrayLocation() {
    for (Criterion c : criteria) {
      if (c.getKind() == Criterion.Kind.GENERIC_ARRAY_LOCATION) {
        return (GenericArrayLocationCriterion) c;
      }
    }
    return null;
  }

  // Returns the last one.  Should really return the outermost one, but I
  // think that works for now.
  /**
   * @return an InClassCriterion if this has one, else null
   */
  public InClassCriterion getInClass() {
    InClassCriterion result = null;
    for (Criterion c : criteria) {
      if (c.getKind() == Criterion.Kind.IN_CLASS) {
        result = (InClassCriterion) c;
      }
    }
    return result;
  }

  /**
   * @return true if this is on the zeroth bound of a type
   */
  // Used when determining whether an annotation is on an implicit upper
  // bound (the "extends Object" that is customarily omitted).
  public boolean onBoundZero() {
    for (Criterion c : criteria) {
      if ((c.getKind() == Criterion.Kind.CLASS_BOUND)
          && ((ClassBoundCriterion) c).boundLoc.boundIndex == 0) {
        return true;
      }
      if ((c.getKind() == Criterion.Kind.METHOD_BOUND)
          && ((MethodBoundCriterion) c).boundLoc.boundIndex == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return criteria.toString();
  }


  ///////////////////////////////////////////////////////////////////////////
  /// Factory methods
  ///

  /**
   * Creates an "is" criterion: that a program element has the specified
   * kind and name.
   *
   * @param kind the program element's kind
   * @param name the program element's name
   * @return an "is" criterion
   */
  public final static Criterion is(Tree.Kind kind, String name) {
    return new IsCriterion(kind, name);
  }

  /**
   * Creates an "enclosed by" criterion: that a program element is enclosed
   * by the specified kind of program element.
   *
   * @param kind the kind of enclosing program element
   * @return an "enclosed by" criterion
   */
  public final static Criterion enclosedBy(Tree.Kind kind) {
    return new EnclosedByCriterion(kind);
  }

  /**
   * Creates an "in package" criterion: that a program element is enclosed
   * by the specified package.
   *
   * @param name the name of the enclosing package
   * @return an "in package" criterion
   */
  public final static Criterion inPackage(String name) {
    return new InPackageCriterion(name);
  }

  /**
   * Creates an "in class" criterion: that a program element is enclosed
   * by the specified class.
   *
   * @param name the name of the enclosing class
   * @param exact whether to match only in the class itself, not in its inner classes
   * @return an "in class" criterion
   */
  public final static Criterion inClass(String name, boolean exact) {
    return new InClassCriterion(name, /*exactmatch=*/ true);
  }

  public final static boolean isClassEquiv(Tree tree) {
    switch (tree.getKind()) {
      case CLASS:
      case NEW_CLASS:
      case INTERFACE:
      case ENUM:
      case ANNOTATION_TYPE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Creates an "in method" criterion: that a program element is enclosed
   * by the specified method.
   *
   * @param name the name of the enclosing method
   * @return an "in method" criterion
   */
  public final static Criterion inMethod(String name) {
    return new InMethodCriterion(name);
  }

  /**
   * Creates a "not in method" criterion: that a program element is not
   * enclosed by any method.
   *
   * @return a "not in method" criterion
   */
  public final static Criterion notInMethod() {
    return new NotInMethodCriterion();
  }

  public final static Criterion packageDecl(String packageName) {
    return new PackageCriterion(packageName);
  }

  public final static Criterion atLocation() {
    return new GenericArrayLocationCriterion();
  }

  public final static Criterion atLocation(InnerTypeLocation loc) {
    return new GenericArrayLocationCriterion(loc);
  }

  public final static Criterion field(String varName) {
	return new FieldCriterion(varName);
  }

  public final static Criterion inStaticInit(int blockID) {
    return new InStaticInitCriterion(blockID);
  }

  public final static Criterion inFieldInit(String varName) {
    return new InFieldInitCriterion(varName);
  }

  public final static Criterion receiver(String methodName) {
    return new ReceiverCriterion(methodName);
  }

  public final static Criterion returnType(String methodName) {
    return new ReturnTypeCriterion(methodName);
  }

  public final static Criterion isSigMethod(String methodName) {
    return new IsSigMethodCriterion(methodName);
  }


  public final static Criterion param(String methodName, Integer pos) {
    return new ParamCriterion(methodName, pos);
  }
//
//  public final static Criterion param(String methodName, Integer pos, InnerTypeLocation loc) {
//    return new ParamCriterion(methodName, pos, loc);
//  }


  public final static Criterion local(String methodName, LocalLocation loc) {
    return new LocalVariableCriterion(methodName, loc);
  }

  public final static Criterion cast(String methodName, RelativeLocation loc) {
    return new CastCriterion(methodName, loc);
  }

  public final static Criterion newObject(String methodName, RelativeLocation loc) {
    return new NewCriterion(methodName, loc);
  }

  public final static Criterion instanceOf(String methodName, RelativeLocation loc) {
    return new InstanceOfCriterion(methodName, loc);
  }

  public final static Criterion atBoundLocation(BoundLocation loc) {
    return new BoundLocationCriterion(loc);
  }

  public final static Criterion atExtImplsLocation(String className, TypeIndexLocation loc) {
    return new ExtImplsLocationCriterion(className, loc);
  }

  public final static Criterion methodBound(String methodName, BoundLocation boundLoc) {
    return new MethodBoundCriterion(methodName, boundLoc);
  }

  public final static Criterion classBound(String className, BoundLocation boundLoc) {
    return new ClassBoundCriterion(className, boundLoc);
  }

}
