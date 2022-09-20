package annotations.tests.executable;

import java.io.*;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.checkerframework.afu.scenelib.annotations.el.AScene;
import org.checkerframework.afu.scenelib.annotations.el.DefException;
import org.checkerframework.afu.scenelib.annotations.io.IndexFileWriter;
import org.checkerframework.afu.scenelib.annotations.io.JavapParser;
import org.checkerframework.afu.scenelib.annotations.io.ParseException;
import org.plumelib.util.FileIOException;

public class JavapDemo {
    public static void main(String[] args) throws IOException, FileIOException, DefException {
        @NonNull AScene scene = new AScene();

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
