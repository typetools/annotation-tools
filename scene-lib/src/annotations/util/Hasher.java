package annotations.util;

/*>>>
import checkers.nullness.quals.*;
*/

/**
 * A simple class to mash a lot of data into a hash code for use in
 * implementing {@link Object#hashCode}.  The mashing algorithm currently is
 * not very good; the advantage of a {@link Hasher} class is that an
 * improvement to its mashing algorithm benefits all classes that use it.
 */
public final class Hasher {
    /**
     * The calculated hash code for the data that has been contributed so far.
     */
    public int hash = 0;

    private static final int SHIFT = 5;

    /**
     * Contributes the data <code>x</code> to the calculation of the hash code.
     */
    public void mash(int x) {
        hash = ((hash << SHIFT) | (hash >> (32 - SHIFT))) ^ x;
    }
}
