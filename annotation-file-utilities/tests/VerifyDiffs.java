/**
 * Little program that checks to see if every file ending in .diff in
 * the current directory is empty.  For each file, displays whether or not
 * it is empty.  If any file is non-empty, exits with an error exit status,
 * else exits with a 0 exit status.
 *
 * Usage: java VerifyDiffs [--show_all]
 *
 * If --show_all option is used, all tests that pass will also be displayed.
 * If --show_all is not used, and all tests pass, then there will be no
 * output.
 */
import java.io.*;

public class VerifyDiffs {
    private static boolean show_all = false;

    private static void parseArgs(String[] args) {
        for(String s : args) {
            if(s.equals("--show_all")) {
                VerifyDiffs.show_all = true;
            }
        }
    }

    public static void main(String[] args) {
        parseArgs(args);

        int passCount = 0;
        int failCount = 0;
        boolean pass = true;
        try {

            File dir = new File(".");

            for(File f : dir.listFiles()) {
                if(f.toString().endsWith(".diff")) {
                    FileReader fr = new FileReader(f);
                    if(fr.read() != -1) { // if not empty, output error message
                        System.out.println(f.toString() + " ...FAILED");
                        pass = false;
                        failCount++;
                    } else {
                        if(VerifyDiffs.show_all) {
                            System.out.println(f.toString() + " ...OK");
                        }
                        passCount++;
                    }
                    fr.close();
                }
            }
        } catch(Exception e) {
            System.out.println("verify diffs failed due to exception: "
                               + e.getMessage());
            pass = false;
        }

        System.out.println("Passed: " + passCount + "    Failed: " + failCount);
        if(pass) {
            if(VerifyDiffs.show_all) {
                System.out.println("All tests succeeded.");
            }
        } else {
            System.out.println("Tests failed.");
            System.exit(1);
        }
    }
}
