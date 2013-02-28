package org.objectweb.asm;

/**
 * Shared methods between {@link ClassVisitor}, {@link FieldVisitor}, and
 * {@link MethodVisitor}.
 */
public interface MemberVisitor {

    /**
     * Visits a type annotation of this member.
     *
     * @param desc the class descriptor of the annotation class.
     * @param visible <tt>true</tt> if the annotation is visible at runtime.
     * @param inCode <tt>true</tt> if this annotation belongs in the <tt>Code</tt>
     *               attribute, <tt>false</tt> otherwise. This is only used for methods.
     * @return a non null visitor to visit the annotation values.
     */
    TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible, boolean inCode);
}
