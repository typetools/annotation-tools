package annotations.tests.executable;

import java.io.*;

import org.checkerframework.afu.scenelib.annotations.el.AScene;
import org.checkerframework.afu.scenelib.annotations.el.DefException;
import org.checkerframework.afu.scenelib.annotations.io.IndexFileWriter;
import org.checkerframework.afu.scenelib.annotations.io.classfile.ClassFileReader;

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
