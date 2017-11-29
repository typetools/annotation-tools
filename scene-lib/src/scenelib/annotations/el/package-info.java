/**
 * <code>annotations.el</code> provides classes that associate annotations with
 * Java elements.  {@link scenelib.annotations.el.AElement}s represent Java elements
 * of the scene that can carry annotations. There is a multi-level class
 * hierarchy for elements that exploits certain commonalities: for example, all
 * and only {@link scenelib.annotations.el.ADeclaration declarations} contain an
 * {@link scenelib.annotations.el.ADeclaration#insertTypecasts insertTypecasts} field.
 * A &ldquo;scene&rdquo; ({@link scenelib.annotations.el.AScene}) contains many elements
 * and represents all the annotations on a set of classes and packages.
 *
 * One related utility class that is important to understand is
 * {@link scenelib.annotations.util.coll.VivifyingMap}, a Map implementation that allows
 * empty entries (for some user-defined meaning of
 * {@link scenelib.annotations.util.coll.VivifyingMap#isEmpty() empty}) and provides a
 * {@link scenelib.annotations.util.coll.VivifyingMap#prune() prune} method to eliminate
 * these entries.  The only way to create any element is to invoke
 * {@link scenelib.annotations.util.coll.VivifyingMap#vivify(Object) vivify()} on a
 * {@link scenelib.annotations.util.coll.VivifyingMap} static member of the appropriate
 * {@link scenelib.annotations.el.AElement} superclass.
 */
package scenelib.annotations.el;
