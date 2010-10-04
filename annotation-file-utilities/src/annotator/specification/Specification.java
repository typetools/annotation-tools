package annotator.specification;

import java.util.List;

// import annotations.io.FileIOException;
import annotator.find.Insertion;

import plume.FileIOException;

/**
 * Represents a file containing a "specification" for placing annotations on
 * program elements.
 */
public interface Specification {

    /**
     * Parses the specification file.
     *
     * @return the insertions that the annotator should make, as determined from
     *         parsing the specification file
     */
    List<Insertion> parse() throws FileIOException;
}
