package annotations.tests.executable;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

import java.io.*;

import plume.FileIOException;

import scenelib.annotations.el.*;
import scenelib.annotations.io.*;

public class JavapDemo {
    public static void main(String[] args) throws IOException, FileIOException, DefException {
        /*@NonNull*/ AScene scene = new AScene();

        String filename = args[0];
        LineNumberReader lnr = new LineNumberReader(new FileReader(filename));
        try {
            JavapParser.parse(new FileReader(filename), scene);
        } catch (ParseException e) {
            throw new FileIOException(lnr, filename, e);
        }

        IndexFileWriter.write(scene, new OutputStreamWriter(System.out));
    }
}
