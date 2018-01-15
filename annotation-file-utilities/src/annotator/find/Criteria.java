package annotator.find;

import java.util.LinkedHashMap;
import java.util.Map;

import scenelib.annotations.el.BoundLocation;
import scenelib.annotations.el.InnerTypeLocation;
import scenelib.annotations.el.LocalLocation;
import scenelib.annotations.el.RelativeLocation;
import scenelib.annotations.el.TypeIndexLocation;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.DebugWriter;
import annotator.Main;

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
  public static DebugWriter dbug = new DebugWriter();

  /** The set of criterion objects, indexed by kind. */
  private final Map<Criterion.Kind, Criterion> criteria;

  /**
   * Creates a new {@code Criteria} without any {@code Criterion}.
   */
  public Criteria() {
    this.criteria = new LinkedHashMap<Criterion.Kind, Criterion>();
  }

  /**
   * Add a {@code Criterion} to this {@code Criteria}.
   *
   * @param c the criterion to add
   */
  public void add(Criterion c) {
    criteria.put(c.getKind(), c);
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
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    for (Criterion c : criteria.values()) {
      if (! c.isSatisfiedBy(path, leaf)) {
        dbug.debug("UNsatisfied criterion of type %s:%n    leaf=%s (%s)%n",
            c, c.getClass(), Main.leafString(path));
        return false;
      } else {
        dbug.debug("satisfied criterion of type %s:%n    leaf=%s (%s)%n",
            c, c.getClass(), Main.leafString(path));
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
    for (Criterion c : criteria.values()) {
      if (! c.isSatisfiedBy(path)) {
        dbug.debug("UNsatisfied criterion: %s%n", c);
        return false;
      } else {
        dbug.debug("satisfied criterion: %s%n", c);
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
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.RECEIVER) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether this is the criteria on a package.
   *
   * @return true iff this is the criteria on a package
   */
  public boolean isOnPackage() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.PACKAGE) {
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
    for (Criterion c : criteria.values()) {
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
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.LOCAL_VARIABLE) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether this is the criteria on the RHS of an occurrence
   * of 'instanceof'.
   */
  public boolean isOnInstanceof() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.INSTANCE_OF) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether this is the criteria on an object initializer.
   */
  public boolean isOnNew() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.NEW) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether this is the criteria on a class {@code extends} bound.
   */
  public boolean isOnTypeDeclarationExtendsClause() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.EXTIMPLS_LOCATION) {
        return ((ExtImplsLocationCriterion) c).getIndex() == -1;
      }
    }
    return false;
  }

  /**
   * Returns true if this Criteria is on the given method.
   */
  public boolean isOnMethod(String methodname) {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.IN_METHOD) {
        if (((InMethodCriterion) c).name.equals(methodname)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if this Criteria is on a field declaration.
   */
  public boolean isOnFieldDeclaration() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.FIELD
          && ((FieldCriterion) c).isDeclaration) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether this is the criteria on a variable declaration:  a
   * local variable or a field declaration, but not a formal parameter
   * declaration.
   *
   * @return true iff this is the criteria on a local variable
   */
  public boolean isOnVariableDeclaration() {
    // Could fuse the loops for efficiency, but is it important to do so?
    return isOnLocalVariable() || isOnFieldDeclaration();
  }


  /**
   * Gives the AST path specified in the criteria, if any.
   *
   * @return AST path from {@link ASTPathCriterion}, or null if none present
   */
  public ASTPath getASTPath() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.AST_PATH) {
        return ((ASTPathCriterion) c).astPath;
      }
    }

    return null;
  }

  /**
   * Returns the name of the class specified in the Criteria, if any.
   *
   * @return class name from {@link InClassCriterion}, or null if none present
   */
  public String getClassName() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.IN_CLASS) {
        return ((InClassCriterion) c).className;
      }
    }

    return null;
  }

  /**
   * Returns the name of the method specified in the Criteria, if any.
   *
   * @return method name from {@link InMethodCriterion}, or null if none present
   */
  public String getMethodName() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.IN_METHOD) {
        return ((InMethodCriterion) c).name;
      }
    }

    return null;
  }

  /**
   * Returns the name of the member field specified in the Criteria, if any.
   *
   * @return field name from {@link FieldCriterion}, or null if none present
   */
  public String getFieldName() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.FIELD) {
        return ((FieldCriterion) c).varName;
      }
    }

    return null;
  }

  /**
   * @return a GenericArrayLocationCriterion if this has one, else null
   */
  public GenericArrayLocationCriterion getGenericArrayLocation() {
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.GENERIC_ARRAY_LOCATION) {
        return (GenericArrayLocationCriterion) c;
      }
    }
    return null;
  }

  /**
   * @return a RelativeCriterion if this has one, else null
   */
  public RelativeLocation getCastRelativeLocation() {
    RelativeLocation result = null;
    for (Criterion c : criteria.values()) {
      if (c.getKind() == Criterion.Kind.CAST) {
        result = ((CastCriterion) c).getLocation();
      }
    }
    return result;
  }

  // Returns the last one. Should really return the outermost one.
  // However, there should not be more than one unless all are equivalent.
  /**
   * @return an InClassCriterion if this has one, else null
   */
  public InClassCriterion getInClass() {
    InClassCriterion result = null;
    for (Criterion c : criteria.values()) {
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
    for (Criterion c : criteria.values()) {
      switch (c.getKind()) {
      case CLASS_BOUND:
        if (((ClassBoundCriterion) c).boundLoc.boundIndex != 0) { break; }
        return true;
      case METHOD_BOUND:
        if (((MethodBoundCriterion) c).boundLoc.boundIndex != 0) { break; }
        return true;
      case AST_PATH:
        ASTPath astPath = ((ASTPathCriterion) c).astPath;
        if (!astPath.isEmpty()) {
          ASTPath.ASTEntry entry = astPath.getLast();
          if (entry.childSelectorIs(ASTPath.BOUND)
              && entry.getArgument() == 0) {
            return true;
          }
        }
        break;
      default:
        break;
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

  @Deprecated
  public final static Criterion field(String varName) {
    return new FieldCriterion(varName);
  }

  public final static Criterion field(String varName, boolean isOnDeclaration) {
    return new FieldCriterion(varName, isOnDeclaration);
  }

  public final static Criterion inStaticInit(int blockID) {
    return new InInitBlockCriterion(blockID, true);
  }

  public final static Criterion inInstanceInit(int blockID) {
    return new InInitBlockCriterion(blockID, false);
  }

  public final static Criterion inFieldInit(String varName) {
    return new InFieldInitCriterion(varName);
  }

  public final static Criterion receiver(String methodName) {
    return new ReceiverCriterion(methodName);
  }

  public final static Criterion returnType(String className, String methodName) {
    return new ReturnTypeCriterion(className, methodName);
  }

  public final static Criterion isSigMethod(String methodName) {
    return new IsSigMethodCriterion(methodName);
  }


  public final static Criterion param(String methodName, Integer pos) {
    return new ParamCriterion(methodName, pos);
  }

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

  public static Criterion memberReference(String methodName, RelativeLocation loc) {
    return new MemberReferenceCriterion(methodName, loc);
  }

  public static Criterion methodCall(String methodName, RelativeLocation loc) {
    return new CallCriterion(methodName, loc);
  }

  public final static Criterion typeArgument(String methodName, RelativeLocation loc) {
    return new TypeArgumentCriterion(methodName, loc);
  }

  public final static Criterion lambda(String methodName, RelativeLocation loc) {
    return new LambdaCriterion(methodName, loc);
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

  public final static Criterion astPath(ASTPath astPath) {
    return new ASTPathCriterion(astPath);
  }
}
