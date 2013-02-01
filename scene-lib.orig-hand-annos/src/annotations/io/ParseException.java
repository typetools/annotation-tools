package annotations.io;

/**
 * Thrown when index file or javap parsing fails. Message looks like:
 * 
 * <pre>
 *        20: Expected `@'
 * </pre>
 * 
 * Because of the way the parser is implemented, sometimes the error message
 * isn't very good; in particular, it sometimes says "expected A, B or C"
 * when there are legal tokens other than A, B, and C.
 */
public final class ParseException extends Exception {
    private static final long serialVersionUID = 1152202248L;

    /**
     * The approximate line number at which parsing failed.
     */
    public final int line;

    /**
     * A message describing why parsing failed.
     */
    public final /*@NonNull*/ String msg;

    ParseException(int line, /*@NonNull*/ String msg) {
        super(line + ": " + msg);
        this.line = line;
        this.msg = msg;
    }
}
