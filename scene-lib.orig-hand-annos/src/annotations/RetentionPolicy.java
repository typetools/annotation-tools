package annotations;

/**
 * The {@link RetentionPolicy} of an annotation determines in what forms
 * of a program it is to be retained. This enum is based on
 * <code>java.lang.annotation.RetentionPolicy</code> and serves the purposes of
 * the annotation scene library.
 */
public /*@Unmodifiable*/ enum RetentionPolicy {
    /**
     * Corresponds to <code>java.lang.annotation.RetentionPolicy.CLASS</code>.
     */
    CLASS("invisible", "RuntimeInvisible"),
    
    /**
     * Corresponds to <code>java.lang.annotation.RetentionPolicy.RUNTIME</code>.
     */
    RUNTIME("visible", "RuntimeVisible");
    
    /**
     * The name of the retention policy as it would appear in an index file.
     */
    public final /*@NonNull*/ String ifname;
    
    /**
     * The name prefix of attributes used to store annotations of this retention
     * policy in a class file.
     */
    public final /*@NonNull*/ String attrPrefix;
    
    private RetentionPolicy(/*@NonNull*/ String ifname,
            /*@NonNull*/ String attrPrefix) {
        this.ifname = ifname;
        this.attrPrefix = attrPrefix;
    }
}
