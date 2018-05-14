package annotations.tests.executable;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

import java.io.*;

import scenelib.annotations.el.*;
import scenelib.annotations.io.*;
import scenelib.annotations.io.classfile.*;

/**
 * A(n) <code>ClassToIndexDemo</code> is/represents ...
 */
public class ClassToIndexDemo {
    public static void main(String[] args) throws IOException, DefException {
        AScene s = new AScene();
        ClassFileReader.read(s, args[0]);
        IndexFileWriter.write(s, new OutputStreamWriter(System.out));
    }
}
