package annotations.util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import plume.UtilMDE;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;

/**
 * @author mcarthur
 */
public class JVMNames {
    public static String getJVMClassName(ClassTree classTree) {
        return null;  // TODO
    }

    public static String getJVMMethodName(MethodTree methodTree) {
        ExecutableElement methodElement = ((JCMethodDecl) methodTree).sym;
        StringBuilder builder = new StringBuilder();
        builder.append(methodTree.getName());
        builder.append("(");
        for (VariableElement ve : methodElement.getParameters()) {
            builder.append(UtilMDE.binaryNameToFieldDescriptor(((VarSymbol) ve).asType().toString()));
        }
        builder.append(")");

        String returnType = methodElement.getReturnType().toString();
        // Special case void since UtilMDE doesn't handle it.
        if ("void".equals(returnType)) {
            builder.append("V");
        } else {
            builder.append(UtilMDE.binaryNameToFieldDescriptor(returnType));
        }
        return builder.toString();
    }
}

