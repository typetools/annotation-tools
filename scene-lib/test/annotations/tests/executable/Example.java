package annotations.tests.executable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.tainting.qual.Tainted;
*/

import java.io.*;
import java.util.*;

import scenelib.annotations.*;
import scenelib.annotations.el.*;
import scenelib.annotations.io.*;

/**
 * Prints information about Tainted and NonNull annotations on a given class.
 * Invoke as:
 * <pre>
 * java Example <i>input.jaif</i> <i>ClassToProcess</i> <i>output.jaif</i>
 * </pre>
 */
public class Example {
  public static void main(String [] args) {
    AScene scene;

    if (! new File(args[0]).exists()) {
      try {
        throw new Error(String.format("Cannot find file %s in directory %s",
                                    args[0], new File(".").getCanonicalPath()));
      } catch (IOException e) {
        throw new Error("This can't happen: ", e);
      }
    }

    // System.out.println("Reading in " + args[0]);
    try {
      scene = new AScene();
      IndexFileParser.parseFile(args[0], scene);
    } catch (IOException e) {
      e.printStackTrace(System.err);
      return;
    }

    System.out.println("Processing class " + args[1]);
    // Get a handle on the class
    AClass clazz1 = scene.classes.get(args[1]);
    if (clazz1 == null) {
      System.out.println("Class " + args[1] + " is not mentioned in annotation file " + args[0]);
      return;
    }
    AClass clazz = (AClass) clazz1;

    for (Map.Entry<String, AMethod> me : clazz.methods.entrySet()) {
      AMethod method = me.getValue();

      Annotation rro = method.receiver.type.lookup("Tainted");
      if (rro == null) {
        System.out.println("Method " + me.getKey()
            + " might modify the receiver");
      } else {
        System.out.println("Method " + me.getKey()
            + " must not modify the receiver");
      }

      ATypeElement paramType1 = method.parameters.getVivify(0).type;
      Annotation p1nn = paramType1.lookup("NonNull");
      if (p1nn == null) {
        System.out.println("Annotating type of first parameter of "
            + me.getKey() + " nonnull");

        paramType1.tlAnnotationsHere.add(Annotations.aNonNull);
      }
    }

    // System.out.println("Writing out " + args[2]);
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
