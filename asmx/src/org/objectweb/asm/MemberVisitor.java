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
     * @return a non null visitor to visit the annotation values.
     */
    TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible);
}
