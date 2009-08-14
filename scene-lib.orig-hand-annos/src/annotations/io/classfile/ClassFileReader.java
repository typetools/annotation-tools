package annotations.io.classfile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import annotations.SimpleAnnotation;
import annotations.SimpleAnnotationFactory;
import annotations.el.AScene;
/**
 * A <code> ClassFileReader </code> provides methods for reading in annotations
 *  from a class file into an {@link annotations.el.AScene}.
 */
public class ClassFileReader {
  
  /**
   * Reads the annotations from the class file <code> fileName </code>
   * and inserts them into <code> scene </code>.  
   * <code> fileName </code> should be a file name that can be resolved from
   * the current working directory, which means it should end in ".class" 
   * for standard Java class files.
   * 
   * @param scene the scene into which the annotations should be inserted
   * @param fileName the file name of the class the annotations should be 
   * read from
   * @throws IOException if there is a problem reading from
   * <code> fileName </code>
   */
  public static void read(AScene<SimpleAnnotation> scene, String fileName)
  throws IOException {
    read(scene, new FileInputStream(fileName));
  }
  
  /**
   * Reads the annotations from the class file <code> fileName </code>
   * and inserts them into <code> scene </code>.  
   * 
   * @param scene the scene into which the annotations should be inserted
   * @param in an input stream containing the class that the annotations
   * should be read from
   * @throws IOException if there is a problem reading from <code> in </code>
   */
  public static void read(AScene<SimpleAnnotation> scene, InputStream in) 
  throws IOException {
    read(scene, new ClassReader(in));
  }
  
  public static void read(AScene<SimpleAnnotation> scene, ClassReader cr)
  {
      ClassAnnotationSceneReader<SimpleAnnotation> ca = 
          new ClassAnnotationSceneReader<SimpleAnnotation>(
              scene, SimpleAnnotationFactory.saf);
        
        cr.accept(ca, true);
  }
}
