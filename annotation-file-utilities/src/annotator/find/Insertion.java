package annotator.find;

import java.util.LinkedHashSet;
import java.util.Set;

import plume.Pair;

/**
 * Specifies something that needs to be inserted into a source file, including
 * the "what" and the "where".
 */
public abstract class Insertion {

    public enum Kind {
        ANNOTATION,
        CAST,
        RECEIVER,
        CLOSE_PARENTHESIS
    }

    private final Criteria criteria;
    // If non-null, then try to put annotation on its own line,
    // horizontally aligned with the location.
    private final boolean separateLine;

    /**
     * The package names for the annotations being inserted by this Insertion.
     * This will be empty unless {@link #getText(boolean, boolean)} is called
     * with abbreviate true.
     */
    protected Set<String> packageNames;

    /**
     * Creates a new insertion.
     *
     * @param criteria where to insert the text
     * @param separateLine whether to insert the text on its own
     */
    public Insertion(Criteria criteria, boolean separateLine) {
        this.criteria = criteria;
        this.separateLine = separateLine;
        this.packageNames = new LinkedHashSet<String>();
    }

    /**
     * Gets the insertion criteria.
     *
     * @return the criteria
     */
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * Gets the insertion text (not commented or abbreviated).
     *
     * @return the text to insert
     */
    public String getText() {
        return getText(false, false);
    }

    /**
     * Gets the insertion text.
     *
     * @param comments
     *            if true, Java 8 features will be surrounded in comments.
     * @param abbreviate
     *            if true, the package name will be removed from the annotations.
     *            The package name can be retrieved again by calling the
     *            {@link #getPackageName()} method.
     * @return the text to insert
     */
    public abstract String getText(boolean comments, boolean abbreviate);

    /**
     * Gets the package name.
     *
     * @return The package name of the annotation being inserted by this
     *         Insertion. This will be empty unless
     *         {@link #getText(boolean, boolean)} is called with abbreviate true.
     */
    public Set<String> getPackageNames() {
        return packageNames;
    }

    /**
     * Gets whether the insertion goes on a separate line.
     *
     * @return whether the insertion goes on a separate line
     */
    public boolean getSeparateLine() {
        return separateLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("(nl=%b) @ %s", separateLine, criteria);
    }

    /**
     * Gets the kind of this insertion.
     */
    public abstract Kind getKind();

    /**
     * Removes the leading package.
     *
     * @return given <code>@com.foo.bar(baz)</code> it returns the pair
     *         <code>{ com.foo, @bar(baz) }</code>.
     */
    public static Pair<String, String> removePackage(String s) {
        int nameEnd = s.indexOf("(");
        if (nameEnd == -1) {
            nameEnd = s.length();
        }
        int dotIndex = s.lastIndexOf(".", nameEnd);
        if (dotIndex != -1) {
            String packageName = s.substring(0, nameEnd);
            if (packageName.startsWith("@")) {
                return Pair.of(packageName.substring(1),
                        "@" + s.substring(dotIndex + 1));
            } else {
                return Pair.of(packageName, s.substring(dotIndex + 1));
            }
        } else {
            return Pair.of((String) null, s);
        }
    }
}
