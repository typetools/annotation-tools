package scenelib.annotations.el;

import org.objectweb.asm.Label;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link LocalLocation} holds information about a local variable.
 * As a variable may have multiple lifetimes, we store this information
 * the same way ASM does as a series of parallel arrays:
 * start for Labels holding the bytecode offset to the starts of the variable's lifetime(s)
 * end for Labels holding the bytecode offset to the ends of the variable's lifetime(s)
 * index for ints holding the stack offset for the variable's lifetime(s)
 */
public final class LocalLocation {
    /**
     * The starts of the scopes of the element being visited.
     * Used only for TypeReference#LOCAL_VARIABLE and TypeReference#RESOURCE_VARIABLE.
     */
    public final Label[] start;

    /**
     * The ends of the scopes of the element being visited.
     * Used only for TypeReference#LOCAL_VARIABLE and TypeReference#RESOURCE_VARIABLE.
     */
    public final Label[] end;

    /**
     * The indices of the element being visited in the classfile.
     * Used only for TypeReference#LOCAL_VARIABLE and TypeReference#RESOURCE_VARIABLE.
     */
    public final int[] index;

    /**
     * The name of the local variable being visited.
     */
    public final @Nullable String variableName;

    /**
     * Construct a new LocalLocation.
     *
     * @param variableName the name of the local variable
     * @param index the offset of the variable in the stack frame
     */
    public LocalLocation(String variableName, int index) {
        this(new Label[] {new Label()}, new Label[] {new Label()}, new int[] {index}, variableName);
    }

    /**
     * Construct a new LocalLocation.
     *
     * @param start the code offsets to the starts of the variable's lifetime(s)
     * @param end the code offsets to the ends of the variable's lifetime(s)
     * @param index the stack offsets of the variable's lifetime(s)
     * @param variableName the name of the local variable
     */
    public LocalLocation(Label[] start, Label[] end, int[] index, String variableName) {
        this.start = start;
        this.end = end;
        this.index = index;
        this.variableName = variableName;
    }

    /**
     * Construct a new LocalLocation representing a single scope/lifetime.
     *
     * @param index the offset of the variable in the stack frame
     * @param scopeStart the bytecode offset of the start of the variable's lifetime
     * @param scopeLength the bytecode length of the variable's lifetime
     */
    public LocalLocation(int index, int scopeStart, int scopeLength) {
        // Only being used by Writers, not Readers for now. Should possibly deprecate this in the future.
        // Changes values reflectively.
        this.index = new int[] {index};
        this.start = new Label[] {new Label()};
        this.end = new Label[] {new Label()};
        this.variableName = null;

        try {
            Field flagsField = Label.class.getDeclaredField("flags");
            Field bytecodeOffsetField = Label.class.getDeclaredField("bytecodeOffset");
            Field FLAG_RESOLVED_FIELD = Label.class.getDeclaredField("FLAG_RESOLVED");

            flagsField.setAccessible(true);
            bytecodeOffsetField.setAccessible(true);
            FLAG_RESOLVED_FIELD.setAccessible(true);

            int FLAG_RESOLVED = (Integer) FLAG_RESOLVED_FIELD.get(null);

            short flagsStart = (Short) flagsField.get(start[0]);
            short flagsEnd = (Short) flagsField.get(end[0]);
            flagsStart |= FLAG_RESOLVED;
            flagsEnd |= FLAG_RESOLVED;

            flagsField.set(start[0], flagsStart);
            bytecodeOffsetField.set(start[0], scopeStart);

            flagsField.set(end[0], flagsEnd);
            bytecodeOffsetField.set(end[0], scopeStart + scopeLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the bytecode offset to the start of the scope.
     * @return The bytecode offset to the start of the scope
     */
    public int getScopeStart() {
        try {
            return start[0].getOffset();
        } catch (IllegalStateException e) {
            System.err.println("Labels not resolved: " + Arrays.toString(start));
            return -1;
        }
    }

    /**
     * Returns the length of the scope (in bytes).
     * @return the length of the scope (in bytes)
     */
    public int getScopeLength() {
        // Should this be end[0] instead?
        try {
            return end[end.length - 1].getOffset() - getScopeStart();
        } catch (IllegalStateException e) {
            System.err.println("Labels not resolved: " + Arrays.toString(end));
            return -1;
        }
    }

    /**
     * Returns the local variable index.
     * @return the local variable index
     */
    public int getVarIndex() {
        return index[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalLocation that = (LocalLocation) o;
        return Arrays.equals(start, that.start) &&
            Arrays.equals(end, that.end) &&
            Arrays.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(start);
        result = 31 * result + Arrays.hashCode(end);
        result = 31 * result + Arrays.hashCode(index);
        return result;
    }

    @Override
    public String toString() {
        return "LocalLocation{" +
            "start=" + Arrays.toString(start) +
            ", end=" + Arrays.toString(end) +
            ", index=" + Arrays.toString(index) +
            '}';
    }

//    /**
//     * Returns whether this {@link LocalLocation} equals <code>o</code>; a
//     * slightly faster variant of {@link #equals(Object)} for when the argument
//     * is statically known to be another nonnull {@link LocalLocation}.
//     */
//    public boolean equals(LocalLocation l) {
//        return index == l.index && scopeStart == l.scopeStart
//                && scopeLength == l.scopeLength &&
//                (varName==null || varName.equals(l.varName)) &&
//                varIndex==l.varIndex;
//    }
//
//    /**
//     * This {@link LocalLocation} equals <code>o</code> if and only if
//     * <code>o</code> is another nonnull {@link LocalLocation} and
//     * <code>this</code> and <code>o</code> have equal {@link #index},
//     * {@link #scopeStart}, and {@link #scopeLength}.
//     */
//    @Override
//    public boolean equals(Object o) {
//        return o instanceof LocalLocation
//                && equals((LocalLocation) o);
//    }
//
//    @Override
//    public int hashCode() {
//        if (varName==null) {
//            return Objects.hash(index, scopeStart, scopeLength);
//        } else {
//            return Objects.hash(varName, varIndex);
//        }
//    }
//
//    @Override
//    public String toString() {
//        if (varName==null) {
//            return "LocalLocation(" + index + ", " + scopeStart + ", " + scopeLength + ")";
//        } else {
//            return "LocalLocation(\"" + varName + "\" #" + varIndex + ")";
//        }
//    }
}
