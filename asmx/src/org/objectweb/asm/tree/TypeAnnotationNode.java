package org.objectweb.asm.tree;

import org.objectweb.asm.TypePath;

public class TypeAnnotationNode extends AnnotationNode {
    public TypeAnnotationNode(final int typeRef,
            final TypePath typePath, final String desc) {
        super(desc);
    }
}
