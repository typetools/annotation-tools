package annotations.util;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import plume.UtilMDE;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.regex.Pattern;

/**
 * Class to generate class formatted names from Trees.
 *
 * @author mcarthur
 */
public class JVMNames {


    /**
     * Hacky way of getting the byte code signature for a method.
     * There is probably an API to do this, but I couldn't find it.
     *
     * @param methodTree
     * @return
     */
    public static String getJVMMethodName(MethodTree methodTree) {
        ExecutableElement methodElement = ((JCMethodDecl) methodTree).sym;
        StringBuilder builder = new StringBuilder();
        builder.append(methodTree.getName());
        builder.append("(");
        for (VariableElement ve : methodElement.getParameters()) {
            builder.append(UtilMDE.binaryNameToFieldDescriptor(stripAnnotations(((VarSymbol) ve).asType().tsym.flatName().toString())));
        }
        builder.append(")");

        TypeMirror returnType = methodElement.getReturnType();
        final String returnTypeStr;
        if (methodElement.getReturnType() instanceof DeclaredType) {
            returnTypeStr = stripAnnotations(((Symbol)((DeclaredType)returnType).asElement()).asType().tsym.flatName().toString());
        } else {
            returnTypeStr = stripAnnotations(methodElement.getReturnType().toString());
        }

        // Special case void since UtilMDE doesn't handle it.
        if ("void".equals(returnTypeStr)) {
            builder.append("V");
        } else {
            builder.append(UtilMDE.binaryNameToFieldDescriptor(returnTypeStr));
        }
        return builder.toString();
    }

    // Types with anntations end up looking like:
    // (@sparta/checkers/quals/Source({sparta/checkers/quals/FlowPermission/CAMERA}) :: byte)
    // We just want the base type.
    private static final Pattern REPLACE_PATTERN = Pattern.compile("(\\(@.+?:: ([^\\)]+)\\))");

    /**
     * Remove the annotations from the input method signature.
     * @param inputType
     * @return
     */
    private static String stripAnnotations(String inputType) {
        return REPLACE_PATTERN.matcher(inputType).replaceAll("$2");
    }
}

