package org.objectweb.asm;

// Added so that the Javarifier can get local variable ranges.
/**
 * A visitor to visit a Java method in which the bytecode positions of the
 * instructions and labels are already known.  When an object that has these
 * positions (for example, a {@link ClassReader}) accepts a
 * {@link MethodVisitor}, it should test whether the visitor happens to be a
 * {@link PrecompiledMethodVisitor}; if so, it should supply the positions
 * using {@link #visitCurrentPosition}.
 */
public interface PrecompiledMethodVisitor extends MethodVisitor {
    /**
     * Informs the visitor of the current bytecode position in the method's
     * code.  This position applies to all labels and instructions visited
     * until {@link #visitCurrentPosition} is called again.
     */
    void visitCurrentPosition(int position);
}
