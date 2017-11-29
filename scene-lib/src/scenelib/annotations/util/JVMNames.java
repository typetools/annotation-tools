package scenelib.annotations.util;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import plume.UtilMDE;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Class to generate class formatted names from Trees.
 *
 * @author mcarthur
 */
public class JVMNames {

    /**
     * Converts a MethodTree into a jvml format method signature.
     * There is probably an API to do this, but I couldn't find it.
     *
     * @param methodTree the tree to convert
     * @return a String signature of methodTree in jvml format
     */
    public static String getJVMMethodName(MethodTree methodTree) {
        ExecutableElement methodElement = ((JCMethodDecl) methodTree).sym;
        StringBuilder builder = new StringBuilder();
        String returnTypeStr;
        builder.append(methodTree.getName());
        builder.append("(");

        if (methodElement == null) {
            // use source AST in lieu of symbol table
            List<JCVariableDecl> params = ((JCMethodDecl) methodTree).params;
            JCVariableDecl param = params.head;
            JCExpression typeTree = ((JCMethodDecl) methodTree).restype;
            returnTypeStr = treeToJVMLString(typeTree);
            while (param != null) {
                builder.append(treeToJVMLString(param.vartype));
                params = params.tail;
                param = params.head;
            }
        } else {
            TypeMirror returnType = methodElement.getReturnType();
            returnTypeStr = typeToJvmlString((Type)returnType);
            for (VariableElement ve : methodElement.getParameters()) {
                Type vt = (Type) ve.asType();
                if (vt.getTag() == TypeTag.TYPEVAR) {
                    vt = vt.getUpperBound();
                }
                builder.append(typeToJvmlString(vt));
            }
        }
        builder.append(")");
        builder.append(returnTypeStr);
        return builder.toString();
    }

    /**
     * Converts a method element into a jvml format method signature.
     * There is probably an API to do this, but I couldn't find it.
     *
     * @param methodElement the method element to convert
     * @return a String signature of methodElement in jvml format
     */
    public static String getJVMMethodName(ExecutableElement methodElement) {
        StringBuilder builder = new StringBuilder();
        String returnTypeStr;
        builder.append(methodElement.getSimpleName());
        builder.append("(");
        TypeMirror returnType = methodElement.getReturnType();
        returnTypeStr = typeToJvmlString((Type)returnType);
        for (VariableElement ve : methodElement.getParameters()) {
            Type vt = (Type) ve.asType();
            if (vt.getTag() == TypeTag.TYPEVAR) {
                vt = vt.getUpperBound();
            }
            builder.append(typeToJvmlString(vt));
        }
        builder.append(")");
        builder.append(returnTypeStr);
        return builder.toString();
    }

    /**
     * Create a JVML string for a type.
     * Uses {@link UtilMDE#binaryNameToFieldDescriptor(String)}
     *
     * Array strings are built by recursively converting the component type.
     *
     * @param type the Type to convert to JVML
     * @return the JVML representation of type
     */
    public static String typeToJvmlString(Type type) {
        if (type.getKind() == TypeKind.ARRAY) {
            return "[" + typeToJvmlString((Type) ((ArrayType) type).getComponentType());
        } else if (type.getKind() == TypeKind.INTERSECTION) {
            // replace w/erasure (== erasure of 1st conjunct)
            return typeToJvmlString(type.tsym.erasure_field);
        } else if (type.getKind() == TypeKind.VOID) {
            return "V";  // special case since UtilMDE doesn't handle void
        } else {
            return UtilMDE.binaryNameToFieldDescriptor(type.tsym.flatName().toString());
        }
    }

    /**
     * Create a JVML string for an AST node representing a type.
     *
     * @param typeTree a Tree representing a type
     * @return the JVML representation of type
     */
    private static String treeToJVMLString(Tree typeTree) {
        StringBuilder builder = new StringBuilder();
        treeToJVMLString(typeTree, builder);
        return builder.toString();
    }

    private static void treeToJVMLString(Tree typeTree, StringBuilder builder) {
        // FIXME: not robust in presence of comments
        switch (typeTree.getKind()) {
        case ARRAY_TYPE:
            builder.append('[');
            treeToJVMLString(((ArrayTypeTree) typeTree).getType(), builder);
            break;
        default:
            String str = typeTree.toString();
            builder.append("void".equals(str) ? "V"
                : UtilMDE.binaryNameToFieldDescriptor(typeTree.toString()));
            break;
        }
    }

    public static String jvmlStringToJavaTypeString(String str) {
        return str.equals("V") ? "void"
                : UtilMDE.fieldDescriptorToBinaryName(str);
    }
}
