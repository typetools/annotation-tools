package annotator.find;

import plume.Pair;

/**
 * Specifies something that needs to be inserted into a source file, including
 * the "what" and the "where".
 */
public class Insertion {

    public enum Kind {
        DEFAULT, // The general insertion
        CAST,
        RECEIVER
    }

    private final String text;
    private final Criteria criteria;
    // If non-null, then try to put annotation on its own line,
    // horizontally aligned with the location.
    private final boolean separateLine;

    /**
     * The package name for the annotation being inserted by this Insertion.
     * This will be null unless getText is called with abbreviate true.
     */
    private String packageName;

    /**
     * Creates a new insertion.
     *
     * @param text the text to insert
     * @param criteria where to insert the text
     * @param separateLine whether to insert the text on its own
     */
    public Insertion(String text, Criteria criteria, boolean separateLine) {
        this.text = text;
        this.criteria = criteria;
        this.separateLine = separateLine;
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
     *            if true, the annotation will be surrounded by block comments.
     * @param abbreviate
     *            if true, the package name will be removed from the annotation.
     *            The package name can be retrieved again by calling the
     *            {@link #getPackageName()} method.
     * @return the text to insert
     */
    public String getText(boolean comments, boolean abbreviate) {
        String result = text;
        if (abbreviate) {
            Pair<String, String> ps = removePackage(result);
            packageName = ps.a;
            result = ps.b;
        }
        if (!result.startsWith("@")) {
            throw new Error("Illegal insertion, must start with @: " + result);
        }
        if (comments) {
            return "/*" + result + "*/";
        }
        return result;
    }

    /**
     * Gets the package name.
     *
     * @return The package name of the annotation being inserted by this
     *         Insertion. Returns null if the annotation does not have a package
     *         name or if this method is not called after getText is called with
     *         abbreviate set to true.
     */
    public String getPackageName() {
        return packageName;
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
        return String.format("%s (nl=%b) @ %s", text, separateLine, criteria);
    }

    /**
     * Gets the kind of this insertion.
     */
    public Kind getKind() {
        return Kind.DEFAULT;
    }

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
