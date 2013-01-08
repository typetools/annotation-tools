package annotator.find;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import plume.Pair;
import type.ArrayType;
import type.DeclaredType;
import type.Type;
import type.WildcardType;
import type.WildcardType.BoundKind;

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
     * Gets the insertion text (not commented or abbreviated, and without added
     * leading or trailing whitespace).
     *
     * @return the text to insert
     */
    public String getText() {
        return getText(false, false, true, 0, '\0');
    }

    /**
     * Gets the insertion text with a leading and/or trailing space added based
     * on the values of the {@code gotSeparateLine}, {@code pos}, and
     * {@code precedingChar} parameters.
     *
     * @param comments
     *            if true, Java 8 features will be surrounded in comments.
     * @param abbreviate
     *            if true, the package name will be removed from the annotations.
     *            The package name can be retrieved again by calling the
     *            {@link #getPackageName()} method.
     * @param gotSeparateLine
     *            {@code true} if this insertion is actually added on a separate
     *            line.
     * @param pos
     *            the source position where this insertion will be inserted.
     * @param precedingChar
     *            the character directly preceding where this insertion will be
     *            inserted. This value will be ignored if {@code pos} is 0.
     *
     * @return the text to insert
     */
    public String getText(boolean comments, boolean abbreviate,
            boolean gotSeparateLine, int pos, char precedingChar) {
        String toInsert = getText(comments, abbreviate);
        if (addLeadingSpace(gotSeparateLine, pos, precedingChar)) {
            toInsert = " " + toInsert;
        }
        if (addTrailingSpace(gotSeparateLine)) {
            toInsert = toInsert + " ";
        }
        return toInsert;
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
    protected abstract String getText(boolean comments, boolean abbreviate);

    /**
     * Indicates if a preceding space should be added to this insertion.
     * Subclasses may override this method for custom leading space rules.
     *
     * @param gotSeparateLine
     *            {@code true} if this insertion is actually added on a separate
     *            line.
     * @param pos
     *            the source position where this insertion will be inserted.
     * @param precedingChar
     *            the character directly preceding where this insertion will be
     *            inserted. This value will be ignored if {@code pos} is 0.
     * @return {@code true} if a leading space should be added, {@code false}
     *         otherwise.
     */
    protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
            char precedingChar) {
        // Don't add a preceding space if this insertion is on its own line,
        // it's at the beginning of the file, the preceding character is already
        // whitespace, or the preceding character is the first formal or generic
        // parameter.
        return !gotSeparateLine && pos != 0
                && !Character.isWhitespace(precedingChar)
                && precedingChar != '(' && precedingChar != '<';
    }

    /**
     * Indicates if a trailing space should be added to this insertion.
     * Subclasses may override this method for custom trailing space rules.
     *
     * @param gotSeparateLine
     *            {@code true} if this insertion is actually added on a separate
     *            line.
     * @return {@code} true if a trailing space should be added, {@code false}
     * otherwise.
     */
    protected boolean addTrailingSpace(boolean gotSeparateLine) {
        // Don't added a trailing space if this insertion is on its own line.
        return !gotSeparateLine;
    }

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

    /**
     * Converts the given type to a String. This method can't be in the
     * {@link Type} class because this method relies on the {@link Insertion}
     * class to format annotations, and the {@link Insertion} class is not
     * available from {@link Type}.
     *
     * @param type
     *            the type to convert
     * @param comments
     *            if true, Java 8 features will be surrounded in comments.
     * @param abbreviate
     *            if true, the package name will be removed from the annotations.
     *            The package name can be retrieved again by calling the
     *            {@link #getPackageName()} method.
     * @return the type as a string
     */
    public String typeToString(Type type, boolean comments, boolean abbreviate) {
        StringBuilder result = new StringBuilder();
        for (String annotation : type.getAnnotations()) {
            AnnotationInsertion ins = new AnnotationInsertion(annotation);
            result.append(ins.getText(comments, abbreviate));
            result.append(" ");
            if (abbreviate) {
                packageNames.addAll(ins.getPackageNames());
            }
        }
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            result.append(declaredType.getName());
            List<Type> typeArguments = declaredType.getTypeParameters();
            if (!typeArguments.isEmpty()) {
                result.append('<');
                result.append(typeToString(typeArguments.get(0), comments, abbreviate));
                for (int i = 1; i < typeArguments.size(); i++) {
                    result.append(", ");
                    result.append(typeToString(typeArguments.get(i), comments, abbreviate));
                }
                result.append('>');
            }
            Type innerType = declaredType.getInnerType();
            if (innerType != null) {
                result.append('.');
                result.append(typeToString(innerType, comments, abbreviate));
            }
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            result.append(typeToString(arrayType.getComponentType(), comments, abbreviate));
            result.append("[]");
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            for (String annotation : wildcardType.getAnnotations()) {
                AnnotationInsertion ins = new AnnotationInsertion(annotation);
                result.append(ins.getText(comments, abbreviate));
                result.append(" ");
                if (abbreviate) {
                    packageNames.addAll(ins.getPackageNames());
                }
            }
            result.append(wildcardType.getTypeArgumentName());
            if (wildcardType.getKind() != BoundKind.NONE) {
                result.append(' ');
                result.append(wildcardType.getKind());
                result.append(' ');
                result.append(typeToString(wildcardType.getBound(), comments, abbreviate));
            }
        }
        // There will be extra whitespace at the end if this is only annotations, so trim
        return result.toString().trim();
    }
}
