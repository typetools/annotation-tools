package annotations.tests;

import java.io.*;

import annotations.*;
import annotations.el.*;
import annotations.io.*;

public class RegurgitateDemo {
    public static void main(/*@NonNull*/ String[/*@NonNull*/ /*@ReadOnly*/] args) {
        // String sampleIndexFile = "package pkg: annotation @A: int value class
        // foo: @pkg.A(value=dinglewompus)";
        /*@NonNull*/ SimpleAnnotationFactory sab = SimpleAnnotationFactory.saf;
        /*@NonNull*/ AScene<SimpleAnnotation> scene =
                new AScene<SimpleAnnotation>(sab);
        try {
            /*@NonNull*/ Reader in = new FileReader("test2-2.jann");
            IndexFileParser.parse(in, scene);

            System.out.println("regurgitating:");
            IndexFileWriter.write(scene, new FileWriter("test2-3.jann"));
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
