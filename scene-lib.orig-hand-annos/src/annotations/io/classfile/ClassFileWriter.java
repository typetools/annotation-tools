package annotations.io.classfile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.asm.ClassReader;

import annotations.SimpleAnnotation;
import annotations.el.AScene;

/**
 * A <code> ClassFileWriter </code> provides methods for inserting annotations
 *  from an {@link annotations.el.AScene} into a class file.
 */
public class ClassFileWriter {
  /**
   * Inserts the annotations contained in <code> scene </code> into 
   * the class file contained in <code> fileName </code>.  
   * <code> fileName </code> should be a file name that can be resolved from
   * the current working directory, which means it should end in ".class" 
   * for standard Java class files.  The new class containing the annotations
   * specified by <code> scene </code> will be written out back to 
   * <code> fileName </code>.  If overwrite is true, then whenever an
   * annotations exists on a particular element in both the scene and the 
   * class file, the one from the scene will be used.  Else, if overwrite is
   * false, the existing annotation in the class file will be used.
   * 
   * @param scene the scene containing the annotations to insert into a class
   * @param fileName the file name of the class the annotations should be 
   * inserted
   * @param overwrite whether to overwrite existing annotations
   * @throws IOException if there is a problem reading from or writing to
   * <code> fileName </code>
   */
  public static void insert(
      AScene<SimpleAnnotation> scene, String fileName, boolean overwrite) 
  throws IOException {
    // can't just call other insert, because this closes the input stream
    InputStream in = new FileInputStream(fileName);
    ClassReader cr = new ClassReader(in);
    in.close();
    
    ClassAnnotationSceneWriter<SimpleAnnotation> cw = 
      new ClassAnnotationSceneWriter<SimpleAnnotation>(scene, overwrite);    
    cr.accept(cw, false);

    OutputStream fos = new FileOutputStream(fileName);
    fos.write(cw.toByteArray());
    fos.close();
  }
  
  /**
   * Inserts the annotations contained in <code> scene </code> into 
   * the class file read from <code> in </code>, and writes the resulting
   * class file into <code> out </code>.  <code> in </code> should be a stream 
   * of bytes that specify a valid Java class file, and <code> out </code> will
   * contain a stream of bytes in the same format, and will also contain the
   * annotations from <code> scene </code>.  If overwrite is true, then whenever 
   * an annotations exists on a particular element in both the scene and the 
   * class file, the one from the scene will be used.  Else, if overwrite is
   * false, the existing annotation in the class file will be used.
   * 
   * @param scene the scene containing the annotations to insert into a class
   * @param in the input stream from which to read a class
   * @param out the output stream the merged class should be written to
   * @param overwrite whether to overwrite existing annotations
   * @throws IOException if there is a problem reading from <code> in </code> or
   * writing to <code> out </code>
   */
  public static void insert(AScene<SimpleAnnotation> scene, InputStream in, 
      OutputStream out, boolean overwrite) throws IOException {
      ClassReader cr = new ClassReader(in);
      
      ClassAnnotationSceneWriter<SimpleAnnotation> cw = 
        new ClassAnnotationSceneWriter<SimpleAnnotation>(scene, overwrite);
      
      cr.accept(cw, false);

      out.write(cw.toByteArray());
  }
}