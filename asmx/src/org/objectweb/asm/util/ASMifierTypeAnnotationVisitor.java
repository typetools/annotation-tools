package org.objectweb.asm.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypeAnnotationVisitor;

/**
 * An {@link TypeAnnotationVisitor} that prints the ASM code that generates 
 *  the extended annotations it visits.
 * 
 * @author jaimeq
 */
public class ASMifierTypeAnnotationVisitor extends AbstractVisitor 
  implements TypeAnnotationVisitor
{

    /**
     * Identifier of the extended annotation visitor variable in the 
     *  produced code.
     */
    protected final int id;

    /**
     * Constructs a new {@link ASMifierTypeAnnotationVisitor}.
     * 
     * @param id identifier of the extended annotation visitor variable in the
     *  produced code.
     */
    public ASMifierTypeAnnotationVisitor(final int id) {
        this.id = id;
    }

    // ------------------------------------------------------------------------
    // Implementation of the TypeAnnotationVisitor interface
    // ------------------------------------------------------------------------

    public void visit(final String name, final Object value) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visit(");
        ASMifierAbstractVisitor.appendConstant(buf, name);
        buf.append(", ");
        ASMifierAbstractVisitor.appendConstant(buf, value);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitEnum(
        final String name,
        final String desc,
        final String value)
    {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitEnum(");
        ASMifierAbstractVisitor.appendConstant(buf, name);
        buf.append(", ");
        ASMifierAbstractVisitor.appendConstant(buf, desc);
        buf.append(", ");
        ASMifierAbstractVisitor.appendConstant(buf, value);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public AnnotationVisitor visitAnnotation(
        final String name,
        final String desc)
    {
        buf.setLength(0);
        buf.append("{\n");
        buf.append("AnnotationVisitor av").append(id + 1).append(" = av");
        buf.append(id).append(".visitAnnotation(");
        ASMifierAbstractVisitor.appendConstant(buf, name);
        buf.append(", ");
        ASMifierAbstractVisitor.appendConstant(buf, desc);
        buf.append(");\n");
        text.add(buf.toString());
        ASMifierAnnotationVisitor av = new ASMifierAnnotationVisitor(id + 1);
        text.add(av.getText());
        text.add("}\n");
        return av;
    }

    public AnnotationVisitor visitArray(final String name) {
        buf.setLength(0);
        buf.append("{\n");
        buf.append("AnnotationVisitor av").append(id + 1).append(" = av");
        buf.append(id).append(".visitArray(");
        ASMifierAbstractVisitor.appendConstant(buf, name);
        buf.append(");\n");
        text.add(buf.toString());
        ASMifierAnnotationVisitor av = new ASMifierAnnotationVisitor(id + 1);
        text.add(av.getText());
        text.add("}\n");
        return av;
    }

    public void visitEnd() {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitEnd();\n");
        text.add(buf.toString());
    }

    public void appendConstant(final StringBuffer buf, int i) {
        ASMifierAbstractVisitor.appendConstant(buf, Integer.valueOf(i));
    }

    public void visitXTargetType(int target_type) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXTargetType(");
        appendConstant(buf, target_type);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXOffset(int offset) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXOffset(");
        appendConstant(buf, offset);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXLocationLength(int location_length) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXLocationLength(");
        appendConstant(buf, location_length);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXLocation(int location) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXLocation(");
        appendConstant(buf, location);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXNumEntries(int num_entries) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXNumEntries(");
        appendConstant(buf, num_entries);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXStartPc(int start_pc) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXStartPc(");
        appendConstant(buf, start_pc);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXLength(int length) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXLength(");
        appendConstant(buf, length);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXIndex(int index) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXIndex(");
        appendConstant(buf, index);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXParamIndex(int param_index) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXParamIndex(");
        appendConstant(buf, param_index);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXBoundIndex(int bound_index) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXBoundIndex(");
        appendConstant(buf, bound_index);
        buf.append(");\n");
        text.add(buf.toString());
    }

    public void visitXTypeIndex(int type_index) {
        buf.setLength(0);
        buf.append("xav").append(id).append(".visitXTypeIndex(");
        appendConstant(buf, type_index);
        buf.append(");\n");
        text.add(buf.toString());
    }
}
