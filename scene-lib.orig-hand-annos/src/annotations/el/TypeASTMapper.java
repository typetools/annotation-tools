package annotations.el;

import java.util.*;

import annotations.*;

/**
 * A {@link TypeASTMapper} traverses a client-maintained abstract syntax
 * tree representing a type in parallel with an {@link ATypeElement} from
 * the annotation scene library, indicating corresponding pairs of AST nodes and
 * {@link AElement}s to the client so the client can process them in some
 * fashion.
 * 
 * <p>
 * To use {@link TypeASTMapper}, write a subclass for your particular AST.
 * Implement {@link #getElementType}, {@link #numTypeArguments}, and
 * {@link #getTypeArgument} so that the mapper knows how to traverse your
 * AST; implement {@link #map} to perform whatever processing you desire on
 * each AST node and its corresponding {@link AElement}.  Then, pass the root
 * of your AST and the corresponding {@link ATypeElement} from your
 * annotation scene to {@link #traverse}.
 * 
 * <p>
 * {@link TypeASTMapper} itself saves no state, but subclasses may save state
 * if they wish.
 * 
 * @param <A> common supertype of the annotations stored in the
 * {@link ATypeElement} objects that will be traversed
 * @param <N> common supertype of the AST nodes
 */
public abstract class TypeASTMapper<A extends Annotation, N> {
    /**
     * Constructs a {@link TypeASTMapper}.  {@link TypeASTMapper}s store no
     * state.
     */
    protected TypeASTMapper() {
    }
    
    private static <A extends Annotation> /*@NonNull*/ AElement<A>
        getInnerType(/*@NonNull*/ ATypeElement<A> te,
                /*@ReadOnly*/ /*@NonNull*/ List</*@NonNull*/ Integer> ls) {
        if (ls.isEmpty())
            return te;
        else
            return te.innerTypes.vivify(new InnerTypeLocation(ls));
    }
    
    /**
     * Traverses the type AST rooted at <code>tastRoot</code>, calling
     * {@link #map} with each AST node and the corresponding {@link AElement}
     * from <code>aslRoot</code>.
     * 
     * If a node of the AST has no corresponding inner type in
     * <code>aslRoot</code>, an inner type {@link AElement} is vivified to hold
     * any annotations that {@link #map} might wish to store in it.  Thus,
     * a call to {@link #traverse} may write to <code>aslRoot</code> even if
     * {@link #map} does not write to its {@link AElement} argument.  You
     * may wish to {@linkplain AElement#prune prune} <code>aslRoot</code> after
     * traversal.
     */
    public void traverse(/*@NonNull*/ N tastRoot,
            /*@NonNull*/ ATypeElement<A> aslRoot) {
        // Elements are added and removed from the end of this sole mutable
        // list during the traversal.
        /*@NonNull*/ List</*@NonNull*/ Integer> ls =
            new ArrayList</*@NonNull*/ Integer>();
        traverse1(tastRoot, aslRoot, ls);
    }
    
    // "Sane": top-level or type argument
    private void traverse1(/*@NonNull*/ N n, /*@NonNull*/ ATypeElement<A> te,
            /*@NonNull*/ List</*@NonNull*/ Integer> ls) {
        N elType = getElementType(n);
        if (elType == null) {
            // no array, so the prefix corresponds to the type right here
            map(n, getInnerType(te, ls));
            int nta = numTypeArguments(n);
            for (int tai = 0; tai < nta; tai++) {
                ls.add(tai);
                traverse1(getTypeArgument(n, tai), te, ls);
                ls.remove(ls.size() - 1);
            }
        } else {
            // at least one array layer to confuse us
            int layers = 0;
            while ((elType = getElementType(n)) != null) {
                ls.add(layers);
                map(n, getInnerType(te, ls));
                ls.remove(ls.size() - 1);
                n = (/*@NonNull*/ N) elType;
                layers++;
            }
            // n is now the innermost element type
            // map it to the prefix
            map(n, getInnerType(te, ls));
            // hack for type arguments of the innermost element type
            ls.add(0);
            int nta = numTypeArguments(n);
            for (int tai = 0; tai < nta; tai++) {
                ls.add(tai);
                traverse1(getTypeArgument(n, tai), te, ls);
                ls.remove(ls.size() - 1);
            }
            ls.remove(ls.size() - 1);
        }
    }
    
    /**
     * If <code>n</code> represents an array type, {@link #getElementType}
     * returns the node for the element type of the array; otherwise it returns
     * <code>null</code>.
     */
    protected abstract N getElementType(/*@NonNull*/ N n);

    /**
     * If <code>n</code> represents a parameterized type,
     * {@link #numTypeArguments} returns the number of type arguments;
     * otherwise it returns 0.
     */
    protected abstract int numTypeArguments(/*@NonNull*/ N n);

    /**
     * Returns the node corresponding to the type argument of <code>n</code>
     * (which must be a parameterized type) at the given index.  The caller
     * must ensure that
     * <code>0 &lt;= index &lt; {@link #numTypeArguments}(n)</code>.
     */
    protected abstract /*@NonNull*/ N getTypeArgument(/*@NonNull*/ N n, int index);

    /**
     * Signals to the client that <code>n</code> corresponds to <code>e</code>.
     * The client may, for example, set flags in <code>n</code> based on the
     * annotations in
     * <code>e.{@link AElement#tlAnnotationsHere tlAnnotationsHere}</code>.
     * The {@link TypeASTMapper} calls {@link #map} on <code>n</code> before it
     * calls {@link #map} on sub-nodes of <code>n</code> but not necessarily
     * before it explores the structure of <code>n</code>'s subtree.
     */
    protected abstract void map(/*@NonNull*/ N n, /*@NonNull*/ AElement<A> e);
}
