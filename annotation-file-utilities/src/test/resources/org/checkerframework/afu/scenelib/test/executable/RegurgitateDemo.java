package annotations.tests.executable;

import java.io.*;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.checkerframework.afu.scenelib.annotations.el.AScene;
import org.checkerframework.afu.scenelib.annotations.el.DefException;
import org.checkerframework.afu.scenelib.annotations.io.IndexFileParser;
import org.checkerframework.afu.scenelib.annotations.io.IndexFileWriter;
import org.checkerframework.afu.scenelib.annotations.io.ParseException;

public class RegurgitateDemo {
    public static void main(@NonNull String @NonNull [] args) {
        // String sampleIndexFile = "package pkg: annotation @A: int value class
        // foo: @pkg.A(value=dinglewompus)";
        @NonNull AScene scene = new AScene();
        try {
            LineNumberReader in = new LineNumberReader(new FileReader("test2-2.jaif"));
            IndexFileParser.parse(in, "test2-2.jaif", scene);

            System.out.println("regurgitating:");
            IndexFileWriter.write(scene, new FileWriter("test2-3.jaif"));
        } catch (ParseException p) {
            p.printStackTrace(System.err);
        } catch (DefException p) {
            p.printStackTrace(System.err);
        } catch (IOException e) {
            // won't happen for a StringReader
            assert false;
        }
        // set a breakpoint here to inspect the scene
        System.out.println("finished");
    }
}
