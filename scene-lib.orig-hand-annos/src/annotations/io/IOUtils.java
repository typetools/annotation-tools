package annotations.io;

/**
 * <code>IOUtils</code> has some static methods useful to scene I/O code.
 */
class IOUtils {
    private IOUtils() {
    }
    
    static /*@NonNull*/ String packagePart(/*@NonNull*/ String className) {
        int lastdot = className.lastIndexOf('.');
        return (lastdot == -1) ? "" : className.substring(0, lastdot);
    }

    static /*@NonNull*/ String basenamePart(/*@NonNull*/ String className) {
        int lastdot = className.lastIndexOf('.');
        return (lastdot == -1) ? className : className.substring(lastdot + 1);
    }
}
