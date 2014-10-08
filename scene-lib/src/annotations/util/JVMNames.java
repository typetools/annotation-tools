package annotations.util;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
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
        builder.append(methodTree.getName());
        builder.append("(");

        if (methodElement == null) {
            // how to work around?
            List<JCVariableDecl> params = ((JCMethodDecl) methodTree).params;
            JCVariableDecl param = params.head;
            while (param != null) {
                //builder.append(typeToJvmlString(treeToType(param.vartype)));
                params = params.tail;
                param = params.head;
            }
        } else {
            for (VariableElement ve : methodElement.getParameters()) {
                Type vt = (Type) ve.asType();
                if (vt.getTag() == TypeTag.TYPEVAR) {
                    vt = vt.getUpperBound();
                }
                builder.append(typeToJvmlString(vt));
            }
        }

        builder.append(")");

        TypeMirror returnType = methodElement.getReturnType();
        String returnTypeStr = typeToJvmlString((Type)returnType);

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
     * @return The JVML representation of type
     */
    private static String typeToJvmlString(Type type) {
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
}
