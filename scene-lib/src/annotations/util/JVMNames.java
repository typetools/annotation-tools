package annotations.util;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
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
        builder.append(methodTree.getName());
        builder.append("(");

        for (VariableElement ve : methodElement.getParameters()) {
            builder.append(typeToJvmlString((Type) ve.asType()));
        }

        builder.append(")");

        TypeMirror returnType = methodElement.getReturnType();
        String returnTypeStr = typeToJvmlString((Type)returnType);

        // Special case void since UtilMDE doesn't handle it.
        if ("void".equals(returnTypeStr)) {
            builder.append("V");
        } else {
            builder.append(returnTypeStr);
        }
        return builder.toString();
    }

    /**
     * Create a JVML string for a type.
     * Uses {@link UtilMDE#binaryNameToFieldDescriptor(String)}
     *
     * Array strings are built by recursively converting the component type.
     *
     * @param type the Type to convert to JVML
     * @return The JVML representation of type
     */
    private static String typeToJvmlString(Type type) {
        if (type.getKind() == TypeKind.ARRAY) {
            return "[" + typeToJvmlString((Type) ((ArrayType) type).getComponentType());
        } else {
            return UtilMDE.binaryNameToFieldDescriptor(type.tsym.flatName().toString());
        }
    }
}

