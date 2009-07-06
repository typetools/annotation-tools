package annotations;

import checkers.nullness.quals.*;
import checkers.javari.quals.ReadOnly;

/**
 * The {@link RetentionPolicy} of an annotation determines in what forms
 * of a program it is to be retained. This enum is based on
 * <code>java.lang.annotation.RetentionPolicy</code> and serves the purposes of
 * the annotation scene library.
 */
public /*@ReadOnly*/ enum RetentionPolicy {
    /**
     * Corresponds to <code>java.lang.annotation.RetentionPolicy.CLASS</code>.
     */
    CLASS("invisible", "RuntimeInvisible"),

    /**
     * Corresponds to <code>java.lang.annotation.RetentionPolicy.RUNTIME</code>.
     */
    RUNTIME("visible", "RuntimeVisible"),

    /**
     * Corresponds to <code>java.lang.annotation.RetentionPolicy.SOURCE</code>.
     * Note that attrPrefix is the empty string, since SOURCE annotations
     * should not appear in the classfile.
     */
    SOURCE("source", "");

    /**
     * The name of the retention policy as it would appear in an index file.
     */
    public final String ifname;

    /**
     * The name prefix of attributes used to store annotations of this retention
     * policy in a class file.
     */
    public final String attrPrefix;

    private RetentionPolicy(String ifname,
            String attrPrefix) {
        this.ifname = ifname;
        this.attrPrefix = attrPrefix;
    }

    @Override
    public String toString() {
      return ifname;
    }
}
