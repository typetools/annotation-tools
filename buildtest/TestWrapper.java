import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * TestWrapper is a class that examines JUnit test results and outputs reports
 *  only if one or more tests failed.
 */
public class TestWrapper {

  /**
   * Prints each file listed in args to standard out, only if it
   * contained a failed JUnit test.
   * @param args the file names of the test results to examine
   */
  public static void main(String[] args) {
    for (String filename : args) {
      try {
        if (containsJUnitFailure(filename)) {
          System.out.println();
          System.out.println("Failed tests in: " + filename);
          print(filename);
        }
      } catch (Exception e) {
        System.out.println("Problem reading file " + filename);
        e.printStackTrace(System.out);
      }
    }
  }

  /**
   * Examines the given file and displays it if there are failed tests.
   *
   * @param filename the name of the file to examine.
   */
  private static boolean containsJUnitFailure(String filename) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    String line = in.readLine();
    while (line != null) {
      if (line.contains("FAILED")) {
        in.close();
        return true;
      }
      line = in.readLine();
    }
    in.close();
    return false;
  }

  /**
   * Prints the specified file.
   * @param filename the name of the file to print
   * @throws Exception if an error occurs
   */
  private static void print(String filename) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    String line = in.readLine();
    while (line != null) {
      System.out.println(line);
      line = in.readLine();
    }
    in.close();
  }
}
