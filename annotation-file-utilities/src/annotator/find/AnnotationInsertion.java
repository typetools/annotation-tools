package annotator.find;

import plume.Pair;

/**
 * Specifies an annotation to be inserted into a source file.
 */
public class AnnotationInsertion extends Insertion {

    /**
     * The annotation to insert.
     */
    private final String annotation;

    /**
     * Creates a new insertion.
     *
     * @param annotation the annotation to insert
     * @param criteria where to insert the annotation
     * @param seperateLine whether to insert the annotation on its own
     */
    public AnnotationInsertion(String annotation, Criteria criteria, boolean separateLine) {
        super(criteria, separateLine);
        this.annotation = annotation;
    }

    /**
     * Creates a new insertion with an empty criteria and the text inserted on
     * the same line.
     *
     * @param annotation the text to insert
     */
    public AnnotationInsertion(String annotation) {
        this(annotation, new Criteria(), false);
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
        String result = annotation;
        if (abbreviate) {
            Pair<String, String> ps = removePackage(result);
            String packageName = ps.a;
            result = ps.b;
            if (packageName != null) {
                packageNames.add(packageName);
            }
        }
        if (!result.startsWith("@")) {
            throw new Error("Illegal insertion, must start with @: " + result);
        }
        if (comments) {
            return "/*" + result + "*/";
        }
        return result;
    }

    /** {@inheritDoc} */
    public Kind getKind() {
        return Kind.ANNOTATION;
    }

    public String toString() {
        return annotation + " " + super.toString();
    }
}
