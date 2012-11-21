package annotator;

import java.io.*;
import java.util.*;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;

/**
 * Represents a Java source file. This class provides three major operations:
 * parsing the source file to obtain a syntax tree (via JSR-199), inserting text
 * into the source file at specified offsets, and writing the rewritten source
 * file.
 */
public final class Source {

    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private JavacTask task;
    private StringBuilder source;

    /**
     * Signifies that a problem has occurred with the compiler that produces
     * the syntax tree for this source file.
     */
    public static class CompilerException extends Exception {

        private static final long serialVersionUID = -4751611137146719789L;

        public CompilerException(String message) {
            super(message);
        }
    }

    /**
     * Sets up a compiler for parsing the given Java source file.
     *
     * @throws CompilerException if the input file couldn't be read
     */
    public Source(String src) throws CompilerException, IOException {

        // Get the JSR-199 compiler.
        this.compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            throw new CompilerException("could not get compiler instance");

        // Get the file manager for locating input files.
        this.fileManager = compiler.getStandardFileManager(null, null, null);
        if (fileManager == null)
            throw new CompilerException("could not get file manager");

        Iterable<? extends JavaFileObject> fileObjs = fileManager
            .getJavaFileObjectsFromStrings(Collections.singletonList(src));

        // Compiler options.
        final String[] stringOpts = new String[] { "-g"};
          //TODO: figure out if these options are necessary? "-source", "1.6x"
        List<String> optsList = Arrays.asList(stringOpts);

        // Create a task.
        // This seems to require that the file names end in .java
        CompilationTask cTask =
            compiler.getTask(null, fileManager, null, optsList, null, fileObjs);
        if (!(cTask instanceof JavacTask))
            throw new CompilerException("could not get a valid JavacTask: " + cTask.getClass());
        this.task = (JavacTask)cTask;

        // Read the source file into a buffer.
        source = new StringBuilder();
        FileInputStream in = new FileInputStream(src);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1)
            bytes.write(c);
        source.append(bytes.toString());
    }

    /**
     * Parse the input file, returning a set of Tree API roots (as
     * <code>CompilationUnitTree</code>s).
     *
     * @return the Tree API roots for the input file
     */
    public Set<CompilationUnitTree> parse() {

        try {
            Set<CompilationUnitTree> compUnits = new HashSet<CompilationUnitTree>();

            for (CompilationUnitTree tree : task.parse())
                compUnits.add(tree);
            return compUnits;

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }

        // return Collections.<CompilationUnitTree>emptySet();
    }

    // TODO: Can be a problem if offsets get thrown off by previous insertions?
    /**
     * Inserts the given string into the source file at the given offset.
     * <p>
     *
     * Note that calling this can throw off indices in later parts of the
     * file.  Therefore, when doing multiple insertions, you should perform
     * them from the end of the file forward.
     *
     * @param offset the offset to place the start of the insertion text
     * @param str the text to insert
     */
    public void insert(int offset, String str) {
        source.insert(offset, str);
    }

    public char charAt(int index) {
        return source.charAt(index);
    }

    public String substring(int start, int end) {
        return source.substring(start, end);
    }

    public String getString() {
        return source.toString();
    }

    /**
     * Writes the modified source file to the given stream.
     *
     * @param out the stream for writing the file
     * @throws IOException if the source file couldn't be written
     */
    public void write(OutputStream out) throws IOException {
        out.write(source.toString().getBytes());
        out.flush();
        out.close();
    }

}
