package annotations.tests;

import java.io.*;

import annotations.*;
import annotations.el.*;
import annotations.io.*;

public class JavapDemo {
    public static void main(String[] args) throws IOException, ParseException, DefException {
        /*@NonNull*/ SimpleAnnotationFactory sab = SimpleAnnotationFactory.saf;
        /*@NonNull*/ AScene<SimpleAnnotation> scene =
                new AScene<SimpleAnnotation>(sab);
        
        JavapParser.parse(new FileReader(args[0]), scene);
        IndexFileWriter.write(scene, new OutputStreamWriter(System.out));
    }
}
