package annotations.tests;

import java.io.*;
import java.util.*;

import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.io.*;

// Invoke as: java Example <input.jann> <ClassToProcess> <output.jann>
public class Example {
    public static void main(/*@NonNull*/ String[/*@NonNull*/ /*@ReadOnly*/] args) {
        /*@NonNull*/ AScene<SimpleAnnotation> scene;

        System.out.println("Reading in " + args[0]);
        try {
            scene = new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf);
            IndexFileParser.parse(new FileReader(args[0]), scene);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        } catch (ParseException e) {
            e.printStackTrace(System.err);
            return;
        }

        System.out.println("Processing class " + args[1]);
        // Get a handle on the class
        AClass<SimpleAnnotation> clazz1 = scene.classes.get(args[1]);
        if (clazz1 == null) {
            System.out
                    .println("That class is never mentioned in the index file!");
            return;
        }
        /*@NonNull*/ AClass<SimpleAnnotation> clazz =
                (/*@NonNull*/ AClass<SimpleAnnotation>) clazz1;

        for (/*@NonNull*/ Map.Entry</*@NonNull*/ String, /*@NonNull*/ AMethod<SimpleAnnotation>> me : clazz.methods
                .entrySet()) {
            /*@NonNull*/ AMethod<SimpleAnnotation> method = me.getValue();

            TLAnnotation<SimpleAnnotation> rro =
                    method.receiver.tlAnnotationsHere.lookup("ReadOnly");
            if (rro == null)
                System.out.println("Method " + me.getKey()
                        + " might modify the receiver");
            else
                System.out.println("Method " + me.getKey()
                        + " must not modify the receiver");

            /*@NonNull*/ ATypeElement<SimpleAnnotation> param1 =
                    method.parameters.vivify(0);
            TLAnnotation<SimpleAnnotation> p1nn =
                    param1.tlAnnotationsHere.lookup("NonNull");
            if (p1nn == null) {
                System.out.println("Annotating first parameter of "
                        + me.getKey() + " nonnull");

                /*@NonNull*/ AnnotationDef nonnullDef =
                        new AnnotationDef(
                                "NonNull",
                                Collections
                                        .</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType> emptyMap());
                /*@NonNull*/ SimpleAnnotation p1nn2 =
                        new SimpleAnnotation(
                                nonnullDef,
                                Collections
                                        .</*@NonNull*/ String, /*@NonNull*/ Object> emptyMap());
                param1.tlAnnotationsHere
                        .add(new TLAnnotation<SimpleAnnotation>(
                                new TLAnnotationDef(nonnullDef,
                                        RetentionPolicy.RUNTIME), p1nn2));
            }
        }

        System.out.println("Writing out " + args[2]);
        try {
            IndexFileWriter.write(scene, new FileWriter(args[2]));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        } catch (DefException e) {
            e.printStackTrace(System.err);
            return;
        }

        System.out.println("Success.");
    }
}
