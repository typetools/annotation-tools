package org.objectweb.asmx.tree;

import org.objectweb.asmx.TypePath;

public class TypeAnnotationNode extends AnnotationNode {
    public TypeAnnotationNode(final int typeRef,
            final TypePath typePath, final String desc) {
        super(desc);
    }
}
