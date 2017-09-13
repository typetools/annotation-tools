package annotator.find;

import plume.Pair;

/**
 * Specifies an annotation to be inserted into a source file.
 */
public class AnnotationInsertion extends Insertion {

    /**
     * The annotation text to be inserted into source code, always starts with "@".
     *
     * E.g. An example would be <code>com.foo.Bar(baz)</code>
     */
    private final String fullyQualifiedAnnotationText;
    /**
     * The fully-qualified name of the annotation to be inserted.
     *
     * E.g. Given an annotation <code>com.foo.Bar(baz)</code>,
     * its fully quailified name would be <code>com.foo.Bar</code>.
     */
    private final String fullyQualifiedAnnotationName;

    private String type;
    private boolean generateBound;
    private boolean generateExtends;
    private boolean wasGenerateExtends;

    /**
     * Creates a new insertion.
     *
     * @param fullyQualifiedAnnotationText the annotation text to be inserted into source code;
     *        starts with "@", and must be a fully-qualified name
     * @param criteria where to insert the annotation
     * @param separateLine whether to insert the annotation on its own
     */
    public AnnotationInsertion(String fullyQualifiedAnnotationText, Criteria criteria, boolean separateLine) {
        super(criteria, separateLine);
        assert fullyQualifiedAnnotationText.startsWith("@") : fullyQualifiedAnnotationText;
        // A fully-qualified name in the default package does not contain a period
        // assert fullyQualifiedAnnotationText.contains(".") : fullyQualifiedAnnotationText;
        this.fullyQualifiedAnnotationText = fullyQualifiedAnnotationText;
        this.fullyQualifiedAnnotationName = extractAnnotationFullyQualifiedName();
        type = null;
        generateBound = false;
        generateExtends = false;
        wasGenerateExtends = false;
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

    public boolean isGenerateExtends() {
        return generateExtends;
    }

    public boolean isGenerateBound() {
        return generateBound;
    }

    public void setGenerateExtends(boolean generateExtends) {
        this.generateExtends = generateExtends;
        this.wasGenerateExtends |= generateExtends;
    }

    public void setGenerateBound(boolean b) {
        generateBound = b;
    }

    /**
     * Gets the insertion text.
     *
     * @param comments
     *            if true, the annotation will be surrounded by block comments
     * @param abbreviate
     *            if true, the package name will be removed from the annotation.
     *            The package name can be retrieved again by calling the
     *            {@link #getPackageName()} method.
     * @return the text to insert
     */
    protected String getText(boolean comments, boolean abbreviate) {
        String result = fullyQualifiedAnnotationText;
        if (abbreviate) {
            Pair<String, String> ps = removePackage(result);
            String packageName = ps.a;
            if (packageName != null) {
                packageNames.add(packageName);
                result = ps.b;
            }
        }
        if (!result.startsWith("@")) {
            throw new Error("Illegal insertion, must start with @: " + result);
        }

        // We insert a "new " when annotating a variable initializer that is a
        // bare array expression (e.g., as in "int[] a = {0, 1};")  Since the
        // syntax doesn't permit adding the type annotation in front of the
        // expression, we generate the explicit "new"
        // (as in "int[] a = new int[] {0, 1}") to provide a legal insertion site.

        if (type != null) {
            result = "new " + result + " " + type;
        } else if (generateBound) {
            result += " Object &";
        } else if (generateExtends) {
            result = " extends " + result + " Object";
        }
        return comments ? "/*" + result + "*/" : result;
    }

    /**
     * Returns the fully-qualified name of the annotation, given its string representation.
     * For example, given "@com.foo.Bar(baz)", returns "com.foo.Bar".
     *
     * @param annotation the string representation of the annotation; starts with "@"
     * @return the fully-qualified name of the annotation, given its string representation
     */
    private String extractAnnotationFullyQualifiedName() {
        assert fullyQualifiedAnnotationText.startsWith("@");
        // annotation always starts with "@", so annotation name begins at index 1
        int nameBegin = 1;

        int nameEnd = fullyQualifiedAnnotationText.indexOf("(");
        // If no argument (no parenthesis in string representation), use whole annotation
        if (nameEnd == -1) {
            nameEnd = fullyQualifiedAnnotationText.length();
        }

        return fullyQualifiedAnnotationText.substring(nameBegin, nameEnd);
    }

    /**
     * Gets the raw, unmodified annotation that was passed into the constructor.
     * @return the annotation
     */
    public String getAnnotation() {
        return fullyQualifiedAnnotationText;
    }

    /**
     * Get the fully-qualified name of the annotation.
     *
     * <p>E.g. given <code>@com.foo.Bar(baz)</code>, the fully-qualified name of this annotation
     * is <code>com.foo.Bar</code>.
     * @return the annotation's fully-qualified name
     */
    public String getAnnotationFullyQualifiedName() {
        return fullyQualifiedAnnotationName;
    }

    /** {@inheritDoc} */
    protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
            char precedingChar) {
        if (generateExtends || precedingChar == '.') {
            return false;
        }
        return super.addLeadingSpace(gotSeparateLine, pos, precedingChar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean addTrailingSpace(boolean gotSeparateLine) {
        // Never add a trailing space on a type parameter bound.
        return !wasGenerateExtends && super.addTrailingSpace(gotSeparateLine);
    }

    /** {@inheritDoc} */
    public Kind getKind() {
        return Kind.ANNOTATION;
    }

    public String toString() {
        return fullyQualifiedAnnotationText + " " + super.toString();
    }

    public void setType(String s) {
        this.type = s;
    }
}
