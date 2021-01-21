package scenelib.annotations.el;

import org.objectweb.asm.Label;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link LocalLocation} holds information about a local variable.
 * A variable may have multiple lifetimes.
 * We store this information the same way ASM does, as 3 parallel arrays.
 */
public final class LocalLocation {
    /**
     * The starts of the lifetimes of the element being visited.
     * Used only for TypeReference#LOCAL_VARIABLE and TypeReference#RESOURCE_VARIABLE.
     */
    public final Label[] start;

    /**
     * The ends of the lifetimes of the element being visited.
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
     *
     * This is not part of the abstract state of the LocalLocation:
     * it is not read by equals(), hashCode(), or toString().
     */
    public final @Nullable String variableName;

    /**
     * Construct a new LocalLocation.  This constructor does not assign meaningful
     * values to start or end.  Thus, the getScopeStart and getScopeLenth methods must not
     * be used on the result.
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
     * Only being used by Writers, not Readers for now. Should possibly deprecate this in the future.
     * Changes values reflectively.
     *
     * @param index the offset of the variable in the stack frame
     * @param scopeStart the bytecode offset of the start of the variable's lifetime
     * @param scopeLength the bytecode length of the variable's lifetime
     */
    public LocalLocation(int index, int scopeStart, int scopeLength) {
        Label startLabel = new Label();
        Label endLabel = new Label();

        try {
            Field flagsField = Label.class.getDeclaredField("flags");
            Field bytecodeOffsetField = Label.class.getDeclaredField("bytecodeOffset");
            Field FLAG_RESOLVED_FIELD = Label.class.getDeclaredField("FLAG_RESOLVED");

            flagsField.setAccessible(true);
            bytecodeOffsetField.setAccessible(true);
            FLAG_RESOLVED_FIELD.setAccessible(true);
            int FLAG_RESOLVED = (Integer) FLAG_RESOLVED_FIELD.get(null);

            short flags = (Short) flagsField.get(startLabel);
            flags |= FLAG_RESOLVED;
            flagsField.set(startLabel, flags);
            bytecodeOffsetField.set(startLabel, scopeStart);

            flags = (Short) flagsField.get(endLabel);
            flags |= FLAG_RESOLVED;
            flagsField.set(endLabel, flags);
            bytecodeOffsetField.set(endLabel, scopeStart + scopeLength);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.start = new Label[] {startLabel};
        this.end = new Label[] {endLabel};
        this.index = new int[] {index};
        this.variableName = null;
    }

    /**
     * Returns the bytecode offset to the start of the first scope/lifetime.
     *
     * @return the bytecode offset to the start of the first scope/lifetime
     */
    public int getScopeStart() {
        try {
            return start[0].getOffset();
        } catch (IllegalStateException e) {
            System.err.println("Labels not resolved: " + Arrays.toString(start));
            return -1;
        }
    }

    // This is used only in IndexFileWriter.
    /**
     * Returns the length of all the scopes/lifetimes (in bytes).
     *
     * @return the length of all the scopes/lifetimes (in bytes)
     */
    public int getScopeLength() {
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
        return Objects.hash(Arrays.hashCode(start), Arrays.hashCode(end), Arrays.hashCode(index));
    }

    @Override
    public String toString() {
        return "LocalLocation{" +
            "start=" + Arrays.toString(start) +
            ", end=" + Arrays.toString(end) +
            ", index=" + Arrays.toString(index) +
            '}';
    }
}
