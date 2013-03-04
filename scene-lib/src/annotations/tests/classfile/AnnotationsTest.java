package annotations.tests.classfile;

import checkers.nullness.quals.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.objectweb.asm.ClassReader;

import annotations.Annotation;
import annotations.AnnotationFactory;
import annotations.el.AScene;
import annotations.io.IndexFileParser;
import annotations.io.IndexFileWriter;
import annotations.io.classfile.ClassFileReader;
import annotations.io.classfile.ClassFileWriter;
import annotations.tests.classfile.foo.A;

/**
 * This class is the testing framework for the class file/index file
 * annotations converter.  To add a new test,
 * <ul>
 *  <li>add the class name to array {@link #allTests}
 *  <li>place two files in directory {@link #CLASS_FILE_BASE}:
 *    a .class file (for the base version of the class), an _Expected.class
 *    file (for the annotated version of the class)
 *  <li>place one file in directory {@link #INDEX_FILE_BASE}: a .jann index file.
 *  <li>Add a <code>testc*()</code> method to test against class file and a
 *    <code>testi*()</code> method to test against index file; this is just so
 *     that JUnit has an accurate count of all tests.
 * </ul>
 *
 * Two types of tests are performed:
 * <ul>
 *   <li>Read the annotations from <code>name.jann</code>, insert them into
 *      <code>name.class</code>, write the results to a temporary file
 *      (name_Generated.class), and compare this generated class file with
 *      <code>name_Expected.class</code>, asserting that they have the same
 *      annotations.
 *   <li>Read the annotations from the generated class file, and check them
 *      against the annotations from the index file.
 * </ul>
 */
public class AnnotationsTest extends TestCase {

  /**
   * The directory in which to find the index files to test.
   */
  private static final String INDEX_FILE_BASE =
    "src/annotations/tests/classfile/cases/";

  /**
   * The directory in which to find the class files (both .class and _Generated.class)
   * to test.
   */
  private static final String CLASS_FILE_BASE =
    "test/annotations/tests/classfile/cases/";

  /**
   * An array of all the classes to test.  For each name in this array, there
   * must be a corresponding .jann file in {@link #INDEX_FILE_BASE} and
   * .class and _Expected.class files in {@link #CLASS_FILE_BASE}
   */
  public static final String[] allTests = {
    "TestClassEmpty",
    "TestClassNonEmpty",
    "TestFieldSimple",
    "TestFieldGeneric",
    "TestLocalVariable",
    "TestLocalVariableGenericArray",
    "TestTypecast",
    "TestTypecastGenericArray",
    "TestTypeTest",
    "TestObjectCreation",
    "TestObjectCreationGenericArray",
    "TestMethodReceiver",
    "TestMethodReturnTypeGenericArray"
  };

  /**
   * Constructs a new <code>AnnotationsTest</code> with the given name.
   *
   * @param s the name of this test case.
   */
  public AnnotationsTest(String s) {
    super(s);
  }

  /**
   * Runs all the tests in {@link #allTests} and displays the failure and error
   * counts.
   */
  public static void main(String[] args) {
    TestSuite suite = new TestSuite(AnnotationsTest.class);
    TestResult result = new TestResult();
    suite.run(result);
    System.out.println(
        "AnnotationsTests ran with " + result.failureCount() + " failures and "
        + result.errorCount() + " errors. (" + result.runCount()
        + " successes.)");
  }

  /**
   * Prepends {@link #CLASS_FILE_BASE} to s.
   */
  private String nameClass(String s) {
    return CLASS_FILE_BASE + s;
  }

  /**
   * Prepends {@link #INDEX_FILE_BASE} to s.
   */
  private String nameIndex(String s) {
    return INDEX_FILE_BASE + s;
  }

  /**
   * Writes out scene to filename as an index file.
   *
   * @param filename the file to write to
   * @param scene the scene to write out
   */
  private void writeScene(String filename, AScene scene) {
    try {
      IndexFileWriter.write(scene, filename);
    } catch(Exception e) {
      System.err.println("caught exception: ");
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Reads in the annotations from filename, an index file, into scene.
   *
   * @param filename the index file to read from
   * @param scene the scene to write out to
   */
  private void readScene(String filename, AScene scene) {
    try {
      IndexFileParser.parseFile(filename, scene);
    } catch(Exception e) {
      System.err.println("caught exception: ");
      e.printStackTrace();
      fail("caught exception: " + e.toString());
    }
  }

  /**
   * Reads in the class file from the given filename, inserts the annotations
   * from scene, and writes out the result into the same file.
   *
   * @param filename the class file to insert annotations into
   * @param scene the scene that contains annotations to be inserted
   * @param overwrite whether to overwrite existing annotations.
   */
  private void writeClass(String filename,
      AScene scene, boolean overwrite) {
    writeClass(filename, filename, scene, overwrite);
  }

  /**
   * Like {@link #writeClass(String, AScene, boolean)}, except the class will be read from and written to
   * different files.
   *
   * @param oldFileName the class file to read from
   * @param newFileName the class file to write to
   * @param scene the scene that contains annotations to be inserted
   * @param overwrite whether to overwrite existing annotations.
   */
  private void writeClass(
      String oldFileName,
      String newFileName,
      AScene scene,
      boolean overwrite) {
    try {
      ClassFileWriter.insert(
          scene,
          new FileInputStream(oldFileName),
          new FileOutputStream(newFileName),
          overwrite);
    } catch(Exception e) {
      System.err.println("caught exception: ");
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Reads in the annotations from the class file at filename and inserts them
   * into scene.
   *
   * @param filename the class file to read from
   * @param scene the scene to write to
   */
  private void readClass(String filename,
      AScene scene) {
    try {
      ClassFileReader.read(scene, filename);
    } catch(Exception e) {
      System.err.printf("caught exception while reading %s:%n", new File(filename).getAbsolutePath());
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Creates scene from the annotations in the given index file.
   *
   * @param indexFile the index file to create a scene from
   * @return the scene created from the given index file
   */
  private AScene createScene(String indexFile) {
    AScene scene =
      new AScene();
    readScene(indexFile, scene);
    return scene;
  }

  /**
   * Asserts that the annotations in two class files match.
   * This method will cause this test to fail if there
   * is a mismatch in annotations, or if there is a mismatch in either field
   * or method information that means these classes cannot reasonably be
   * compared.
   *
   * @param correctClass the file name of the correct version of the class
   * @param generatedClass the file name of the version of the class being tested
   */
  private void assertClassAnnotations(String correctClass, String generatedClass) {

    try {
      InputStream correctIs = new FileInputStream(correctClass);

      InputStream generatedIs = new FileInputStream(generatedClass);

      ClassReader crCorrect = new ClassReader(correctIs);
      ClassReader crGenerated = new ClassReader(generatedIs);

      AnnotationVerifier av = new AnnotationVerifier();

      crCorrect.accept(av.originalVisitor(), false);
      crGenerated.accept(av.newVisitor(), false);

      try {
        av.verify();
      } catch(AnnotationVerifier.AnnotationMismatchException e) {
        String message = String.format("assertClassAnnotations (consider running javap on the two .class files):%n  correctClass %s%n  generatedClass %s%n%s", correctClass, generatedClass, e.toString());
        System.out.println();
        System.out.println(message);
        av.verifyPrettyPrint();
        System.out.println(message);
        System.out.println();
        fail(message);
      }

    } catch(IOException e) {
      fail("IOException caught: " + e);
    }
  }

  /**
   * Runs a test that:
   *  <li> reads annotations from indexFileName,
   *  <li> inserts them into baseClassName.class,
   *  <li> writes the result out to baseClassName_Generated.class, and
   *  <li> asserts that the results written out match baseClassName_Expected.class
   */
  private void testAgainstClass(String indexFileName, String baseClassName) {
    String base = baseClassName + ".class";
    String expected = baseClassName + "_Expected.class";
    String generated = baseClassName + "_Generated.class";

    AScene scene = new AScene();

    // read in annotations from index file to scene
    readScene(indexFileName, scene);

    // read in class from base, merge annotations from scene and
    //  write out to generated
    writeClass(base, generated, scene, true);

    // assert that generated class has same annotations as expected class
    assertClassAnnotations(expected, generated);
  }

  /**
   * Runs a test that:
   *  <li> reads annotations from indexFileName,
   *  <li> inserts them into className
   *  <li> writes results out to a temporary class file
   *  <li> reads annotations from that class file, and
   *  <li> asserts that results written out match the annotations in the index file.
   */
  private void testAgainstIndexFile(String indexFileName, String className) {
    AScene correctScene = createScene(indexFileName);

    String basename = className;
    if (basename.endsWith(".class")) {
      basename = basename.substring(0, basename.length() - 6);
    }

    File tempFile = new File(basename+"_temp.class");

    writeClass(className, tempFile.toString(), correctScene, true);

    AScene generatedScene = new AScene();

    readClass(tempFile.toString(), generatedScene);

    correctScene.prune();
    generatedScene.prune();

    if (!correctScene.equals(generatedScene)) {
      String fname1 = className+"-from-indexfile.txt";
      String fname2 = className+"-via-classfile-scene.txt";
      writeScene(fname1, correctScene);
      writeScene(fname2, generatedScene);
      fail(String.format("For annotations read from %s :%n  After writing to class file and re-reading, result differed.%n  Scene read from index file is in %s .%n  Scene generated from class file is in %s .%n  Also consider running javap -v on %s .%n", indexFileName, fname1, fname2, tempFile));
    }

    tempFile.delete();

  }

  /**
   * Runs both types of tests (against class file and index file), on all
   * classes specified by {@link #allTests}
   */
  public void testAll() throws Exception {
//    for (String s : allTests) {
//      testAgainstIndexFile(nameIndex(s + ".jann"), nameClass(s+".class"));
//      testAgainstClass(nameIndex(s + ".jann"), nameClass(s));
//    }
  }

  /**
   * Runs a test on class files for TestClassEmpty.
   */
  public void testcClassEmpty() {
    testAgainstClass(nameIndex("TestClassEmpty.jann"),
        nameClass("TestClassEmpty"));
  }

  /**
   * Runs a test on index files for TestClassEmpty.
   */
  public void testiClassEmpty() {
    testAgainstIndexFile(nameIndex("TestClassEmpty.jann"),
        nameClass("TestClassEmpty.class"));
  }

  /**
   * Runs a test on class files for TestClassNonEmpty.
   */
  public void testcClassNonEmpty() {
    testAgainstClass(nameIndex("TestClassNonEmpty.jann"),
        nameClass("TestClassNonEmpty"));
  }

  /**
   * Runs a test on index files for TestClassNonEmpty.
   */
  public void testiClassNonEmpty() {
    testAgainstIndexFile(nameIndex("TestClassNonEmpty.jann"),
        nameClass("TestClassNonEmpty.class"));
  }

  /**
   * Runs a test on class files for TestFieldSimple.
   */
  public void testcFieldSimple() {
    testAgainstClass(nameIndex("TestFieldSimple.jann"),
        nameClass("TestFieldSimple"));
  }

  /**
   * Runs a test on index files for TestFieldSimple.
   */
  public void testiFieldSimple() {
    testAgainstIndexFile(nameIndex("TestFieldSimple.jann"),
        nameClass("TestFieldSimple.class"));
  }

  /**
   * Runs a test on class files for TestFieldGeneric.
   */
  public void testcFieldGeneric() {
    testAgainstClass(nameIndex("TestFieldGeneric.jann"),
        nameClass("TestFieldGeneric"));
  }

  /**
   * Runs a test on index files for TestFieldGeneric.
   */
  public void testiFieldGeneric() {
    testAgainstIndexFile(nameIndex("TestFieldGeneric.jann"),
        nameClass("TestFieldGeneric.class"));
  }

  /**
   * Runs a test on class files for TestLocalVariable.
   */
  public void testcLocalVariable() {
    testAgainstClass(nameIndex("TestLocalVariable.jann"),
        nameClass("TestLocalVariable"));
  }

  /**
   * Runs a test on index files for TestLocalVariable.
   */
  public void testiLocalVariable() {
    testAgainstIndexFile(nameIndex("TestLocalVariable.jann"),
        nameClass("TestLocalVariable.class"));
  }

  /**
   * Runs a test on class files for TestLocalVariableGenericArray.
   */
  public void testcLocalVariableGenericArray() {
    testAgainstClass(nameIndex("TestLocalVariableGenericArray.jann"),
        nameClass("TestLocalVariableGenericArray"));
  }

  /**
   * Runs a test on index files for TestLocalVariableGenericArray.
   */
  public void testiLocalVariableGenericArray() {
    testAgainstIndexFile(nameIndex("TestLocalVariableGenericArray.jann"),
        nameClass("TestLocalVariableGenericArray.class"));
  }

  /**
   * Runs a test on class files for TestTypecast.
   */
  public void testcTypecast() {
    testAgainstClass(nameIndex("TestTypecast.jann"),
        nameClass("TestTypecast"));
  }

  /**
   * Runs a test on index files for TestTypecast.
   */
  public void testiTypecast() {
    testAgainstIndexFile(nameIndex("TestTypecast.jann"),
        nameClass("TestTypecast.class"));
  }

  /**
   * Runs a test on class files for TestTypecastGenericArray.
   */
  public void testcTypecastGenericArray() {
    testAgainstClass(nameIndex("TestTypecastGenericArray.jann"),
        nameClass("TestTypecastGenericArray"));
  }

  /**
   * Runs a test on index files for TestTypecastGenericArray.
   */
  public void testiTypecastGenericArray() {
    testAgainstIndexFile(nameIndex("TestTypecastGenericArray.jann"),
        nameClass("TestTypecastGenericArray.class"));
  }

  /**
   * Runs a test on class files for TestTypeTest.
   */
  public void testcTypeTest() {
    testAgainstClass(nameIndex("TestTypeTest.jann"),
        nameClass("TestTypeTest"));
  }

  /**
   * Runs a test on index files for TestTypeTest.
   */
  public void testiTypeTest() {
    testAgainstIndexFile(nameIndex("TestTypeTest.jann"),
        nameClass("TestTypeTest.class"));
  }

  /**
   * Runs a test on class files for TestObjectCreation.
   */
  public void testcObjectCreation() {
    testAgainstClass(nameIndex("TestObjectCreation.jann"),
        nameClass("TestObjectCreation"));
  }

  /**
   * Runs a test on index files for TestObjectCreation.
   */
  public void testiObjectCreation() {
    testAgainstIndexFile(nameIndex("TestObjectCreation.jann"),
        nameClass("TestObjectCreation.class"));
  }

  /**
   * Runs a test on class files for TestObjectCreationGenericArray.
   */
  public void testcObjectCreationGenericArray() {
    testAgainstClass(nameIndex("TestObjectCreationGenericArray.jann"),
        nameClass("TestObjectCreationGenericArray"));
  }

  /**
   * Runs a test on index files for TestObjectCreationGenericArray.
   */
  public void testiObjectCreationGenericArray() {
    testAgainstIndexFile(nameIndex("TestObjectCreationGenericArray.jann"),
        nameClass("TestObjectCreationGenericArray.class"));
  }

  /**
   * Runs a test on class files for TestMethodReceiver.
   */
  public void testcMethodReceiver() {
    testAgainstClass(nameIndex("TestMethodReceiver.jann"),
        nameClass("TestMethodReceiver"));
  }

  /**
   * Runs a test on index files for TestMethodReceiver.
   */
  public void testiMethodReceiver() {
    testAgainstIndexFile(nameIndex("TestMethodReceiver.jann"),
        nameClass("TestMethodReceiver.class"));
  }

  /**
   * Runs a test on class files for TestMethodReturnTypeGenericArray.
   */
  public void testcMethodReturnTypeGenericArray() {
    testAgainstClass(nameIndex("TestMethodReturnTypeGenericArray.jann"),
        nameClass("TestMethodReturnTypeGenericArray"));
  }

  /**
   * Runs a test on index files for TestMethodReturnTypeGenericArray.
   */
  public void testiMethodReturnTypeGenericArray() {
    testAgainstIndexFile(nameIndex("TestMethodReturnTypeGenericArray.jann"),
        nameClass("TestMethodReturnTypeGenericArray.class"));
  }

//   // Call javap programmatically.
//   public static void javap(InputStream is, PrintStream ps) {
//     JavapEnvironment env = new JavapEnvironment();
//     PrintWriter pw = new PrintWriter(ps);
//     JavapPrinter javapPrinter = new JavapPrinter(is, pw, env);
//     javapPrinter.print();
//     pw.flush();
//   }

}
