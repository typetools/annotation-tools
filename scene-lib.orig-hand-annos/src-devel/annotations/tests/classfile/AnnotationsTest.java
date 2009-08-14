package annotations.tests.classfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.objectweb.asm.ClassReader;

import annotations.SimpleAnnotation;
import annotations.SimpleAnnotationFactory;
import annotations.el.AScene;
import annotations.io.IndexFileParser;
import annotations.io.IndexFileWriter;
import annotations.io.classfile.ClassFileReader;
import annotations.io.classfile.ClassFileWriter;

/** 
 * This class is the testing framework for the class file/ index file 
 * annotations converter.  {@link #testAll()} runs tests on all classes in 
 * {@link #allTests}, so all that is necessary to add a test for a class is to
 * put the proper .class file (for the base version of the class) and .expected 
 * file (for the annotated version of the class) in {@link #CLASS_FILE_BASE}, 
 * and the right index file in {@link #INDEX_FILE_BASE} and add the class name
 * to {@link #allTests}.  However, in order for JUnit to have an accurate count
 * of all tests, one should also manually add a <code>testc*()</code>
 * method to test against class file and a <code>testi*()</code> method to test
 * against index file.
 *
 * <p>
 *  Each test requires three files: a <code>name.class</code>, 
 *  <code>name.expected</code> and a <code>name.jann</code> file.  The first two
 *  are valid class files, and the third is an index file with annotations for
 *  that class.  Two types of tests are performed.  The first is to read in
 *  the annotations from <code>name.jann</code>, insert them into 
 *  <code>name.class</code>, writing the results out to a temporary file, and
 *  then comparing this generated class file with <code>name.expected</code>,
 *  asserting that they have the same annotations.  The second test is to read
 *  in the annotations from the generated class file, and checking them against
 *  the annotations from the index file.
 * </p>
 */
public class AnnotationsTest extends TestCase {
  
  /**
   * The directory in which to find the index files to test.
   */
  private static final String INDEX_FILE_BASE = 
    "src-devel/annotations/tests/classfile/cases/";
  
  /**
   * The directory in which to find the class files (both .class and .generated)
   * to test.
   */
  private static final String CLASS_FILE_BASE = 
    "test/annotations/tests/classfile/cases/";
  
  /**
   * An array of all the classes to test.  For each name in this array, there
   * must be a corresponding .jann file in {@link #INDEX_FILE_BASE} and 
   * .class and .expected files in {@link #CLASS_FILE_BASE}
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
   * The singleton SimpleAnnotationFactory to use throughout the tests.
   */
  private SimpleAnnotationFactory saf;
  
  /**
   * Performs initial work to set up each test.
   */
  protected void setUp() {
    saf = SimpleAnnotationFactory.saf;
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
  private void writeScene(String filename, AScene<SimpleAnnotation> scene) {
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
  private void readScene(String filename, AScene<SimpleAnnotation> scene) {
    try {
      IndexFileParser.parse(filename, scene);
    } catch(Exception e) {
      System.err.println("caught exception: ");
      e.printStackTrace();
      fail();
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
      AScene<SimpleAnnotation> scene, boolean overwrite) {
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
      AScene<SimpleAnnotation> scene, 
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
      AScene<SimpleAnnotation> scene) {
    try {
      ClassFileReader.read(scene, filename);
    } catch(Exception e) {
      System.err.println("caught exception: ");
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
  private AScene<SimpleAnnotation> createScene(String indexFile) {
    AScene<SimpleAnnotation> scene = 
      new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf);
    readScene(indexFile, scene);
    return scene;
  }
  
  /**
   * Asserts that the annotations in one class file match the annotations in
   * another class file.  This method will cause this test to fail if there
   * is a mismatch in annotations, or if there is a mismatch in either field
   * or method information that means these classes cannot reasonably be 
   * compared.
   * 
   * @param correctClass the file name of the correct version of the class
   * @param generatedClass the file name of the version of the class being tested
   */
  private void assertClassAnnotations(
      String correctClass, String generatedClass) {
    
    try {
      InputStream correctIs = new FileInputStream(correctClass);
      
      InputStream generatedIs = new FileInputStream(generatedClass);
      
      ClassReader crCorrect = new ClassReader(correctIs);
      ClassReader crGenerated = new ClassReader(generatedIs);
      
      AnnotationVerifier av = new AnnotationVerifier();
      
      crCorrect.accept(av.originalVisitor(), false);
      crGenerated.accept(av.newVisitor(), false);
      
      av.verify();
    } catch(IOException e) {
      fail("IOException caught: " + e);
    } catch(AnnotationVerifier.AnnotationMismatchException e) {
      fail(e.toString());
    }
  }
  
  /**
   * Runs a test that reads annotations from <code>indexFileName</code>, inserts
   * them into <code>baseClassName.class</code>, writes the result out to
   * <code>baseClassName.generated</code> and asserts that the results written
   * out match <code>baseClassName.expected</code>.
   */
  private void testAgainstClass(String indexFileName, String baseClassName) {
    String base = baseClassName + ".class";
    String expected = baseClassName + ".expected";
    String generated = baseClassName + ".generated";
    
    AScene<SimpleAnnotation> scene = new AScene<SimpleAnnotation>(saf);
    
    // read in annotations from index file to scene
    readScene(indexFileName, scene);
    
    // read in class from base, merge annotations from scene and
    //  write out to generated
    writeClass(base, generated, scene, true);
    
    // assert that generated class has same annotations as expected class
    assertClassAnnotations(expected, generated);
  }
  
  /**
   * Runs a test that reads annotations from <code>indexFileName</code>, 
   * inserts them into <code>className</code>, writes results out to a 
   * temporary class file, reads in annotations from that class file, 
   * and asserts that results written out match the annotations the index file.
   */
  private void testAgainstIndexFile(String indexFileName, String className) {
    AScene<SimpleAnnotation> correctScene = createScene(indexFileName);
    
    File tempFile = new File(className+".temp");
    
    writeClass(className, tempFile.toString(), correctScene, true);
    
    AScene<SimpleAnnotation> generatedScene = new AScene<SimpleAnnotation>(saf);
    
    readClass(tempFile.toString(), generatedScene);
    
    tempFile.delete();
    
    correctScene.prune();
    generatedScene.prune();
    
    if(!correctScene.equals(generatedScene)) {
      writeScene("test/"+className+"-generated-scene.txt", generatedScene);
      writeScene("test/"+className+"-correct-scene.txt", correctScene);
      fail("Scene generated from class file does not match index file.");
    }
  }
  
  /**
   * Runs both types of tests (against class file and index file), on all
   * classes specified by {@link #allTests}
   */
  public void testAll() throws Exception {
    for(String s : allTests) {
      testAgainstClass(nameIndex(s + ".jann"), nameClass(s));
      testAgainstIndexFile(nameIndex(s + ".jann"), nameClass(s+".class"));
    }
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
}
