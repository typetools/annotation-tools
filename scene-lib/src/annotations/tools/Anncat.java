package annotations.tools;

import checkers.nullness.quals.*;

import java.io.*;

import plume.FileIOException;

import annotations.*;
import annotations.el.*;
import annotations.io.*;
import annotations.io.classfile.*;

import checkers.nullness.quals.*;

/** Concatenates multiple descriptions of annotations into a single one. **/
public class Anncat {
    private static void usage() {
        System.err.print(
                "anncat, part of the Annotation File Utilities\n" +
                "(http://types.cs.washington.edu/annotation-file-utilities/)\n" +
                "usage: anncat <inspec>* [ --out <outspec> ], where:\n" +
                "    <inspec> ::=\n" +
                "        ( --javap <in.javap> )\n" +
                "        | ( --index <in.jann> )\n" +
                "        | ( --class <in.class> )\n" +
                "    <outspec> ::=\n" +
                "        ( --index <out.jann> )\n" +
                "        | ( --class [ --overwrite ] <orig.class> [ --to <out.class> ] )\n" +
        "If outspec is omitted, default is index file to stdout.\n");
    }

    private static void usageAssert(boolean b) {
        if (!b) {
            System.err.println("*** Usage error ***");
            usage();
            System.exit(3);
        }
    }

    public static void main(String[] args) {
        usageAssert(0 < args.length);
        if (args[0].equals("--help")) {
            usage();
            System.exit(0);
        }

        try {

            int idx = 0;

            /*@NonNull*/ AScene theScene =
                new AScene();

            // Read the scene
            while (idx < args.length && !args[idx].equals("--out")) {
                if (args[idx].equals("--javap")) {
                    idx++;
                    usageAssert(idx < args.length);
                    String infile = args[idx++];
                    System.out.println("Reading javap file " + infile + "...");
                    JavapParser.parse(infile, theScene);
                    System.out.println("Finished.");
                } else if (args[idx].equals("--index")) {
                    idx++;
                    usageAssert(idx < args.length);
                    String infile = args[idx++];
                    System.err.println("Reading index file " + infile + "...");
                    IndexFileParser.parseFile(infile, theScene);
                    System.err.println("Finished.");
                } else if (args[idx].equals("--class")) {
                    idx++;
                    usageAssert(idx < args.length);
                    String infile = args[idx++];
                    System.err.println("Reading class file " + infile + "...");
                    ClassFileReader.read(theScene, infile);
                    System.err.println("Finished.");
                } else
                    usageAssert(false);
            }

            // Write the scene
            if (idx == args.length) {
                System.err.println("Writing index file to standard output...");
                IndexFileWriter.write(theScene, new OutputStreamWriter(System.out));
                System.err.println("Finished.");
            } else {
                idx++;
                usageAssert(idx < args.length);
                if (args[idx].equals("--index")) {
                    idx++;
                    usageAssert(idx < args.length);
                    String outfile = args[idx];
                    idx++;
                    usageAssert(idx == args.length);
                    System.err.println("Writing index file to " + outfile + "...");
                    IndexFileWriter.write(theScene, new FileWriter(outfile));
                    System.err.println("Finished.");
                } else if (args[idx].equals("--class")) {
                    idx++;
                    usageAssert(idx < args.length);
                    boolean overwrite;
                    if (args[idx].equals("--overwrite")) {
                        System.err.println("Overwrite mode enabled.");
                        overwrite = true;
                        idx++;
                        usageAssert(idx < args.length);
                    } else
                        overwrite = false;
                    String origfile = args[idx];
                    idx++;
                    if (idx < args.length) {
                        usageAssert(args[idx].equals("--to"));
                        idx++;
                        usageAssert(idx < args.length);
                        String outfile = args[idx];
                        idx++;
                        usageAssert(idx == args.length);
                        System.err.println("Reading original class file " + origfile);
                        System.err.println("and writing annotated version to " + outfile + "...");
                        ClassFileWriter.insert(theScene, new FileInputStream(origfile), new FileOutputStream(outfile), overwrite);
                        System.err.println("Finished.");
                    } else {
                        System.err.println("Rewriting class file " + origfile + " with annotations...");
                        ClassFileWriter.insert(theScene, origfile, overwrite);
                        System.err.println("Finished.");
                    }
                } else
                    usageAssert(false);
            }

        } catch (FileIOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(2);
        } catch (DefException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        System.exit(0);
    }
}
