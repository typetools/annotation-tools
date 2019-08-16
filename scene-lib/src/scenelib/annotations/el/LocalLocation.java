package scenelib.annotations.el;

import org.objectweb.asm.Label;

import java.lang.reflect.Field;
import java.util.Arrays;


/**
 * A {@link LocalLocation} holds location information for a local
 * variable: slot index, scope start, and scope end.
 */
public final class LocalLocation {

    public final Label[] start;
    public final Label[] end;
    public final int[] index;
    public final String variableName;

    // This is made private so that we always call {@link Label#getOffset} whenever we access these. This is to prevent
    // a case where labels passed to the constructor would not be resolved at that point.
    private int scopeStart;
    private int scopeLength;

    public LocalLocation(String variableName, int index) {
        this(new Label(), new Label(), index, variableName);
        System.out.println("BAD1");
    }

    public LocalLocation(Label start, Label end, int index, String variableName) {
        this(new Label[] {start}, new Label[] {end}, new int[] {index}, variableName);
    }

    public LocalLocation(Label[] start, Label[] end, int[] index, String variableName) {
        this.start = start;
        this.end = end;
        this.index = index;
        this.scopeStart = -1; // start[0].getOffset();  // FIXME
        this.scopeLength = -1; // end[end.length - 1].getOffset() - start[0].getOffset(); // FIXME
        this.variableName = variableName;
    }

    public LocalLocation(int index, int scopeStart, int scopeLength) {
        // Only being used by Writers, not Readers for now. Should possibly deprecate this in the future.
        // Changes values reflectively.
        System.out.println("BAD2");
        this.scopeStart = scopeStart;
        this.scopeLength = scopeLength;
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
     * Returns the bytecode offset to the start of the scope
     * @return The bytecode offset to the start of the scope
     */
    public int getScopeStart() {
        scopeStart = start[0].getOffset();
        return scopeStart;
    }

    /**
     * Returns the length of the scope (in bytes)
     * @return The length of the scope (in bytes)
     */
    public int getScopeLength() {
        scopeLength = end[end.length - 1].getOffset() - start[0].getOffset();
        return scopeLength;
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
}
