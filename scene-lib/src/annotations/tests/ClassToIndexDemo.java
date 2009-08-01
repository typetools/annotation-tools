package annotations.tests;

import checkers.nullness.quals.*;

import java.io.*;

import annotations.*;
import annotations.el.*;
import annotations.io.*;
import annotations.io.classfile.*;

/**
 * A(n) <code>ClassToIndexDemo</code> is/represents ...
 */
public class ClassToIndexDemo {
    public static void main(String[] args) throws IOException, DefException {
        AScene s = new AScene();
        ClassFileReader.read(s, args[0]);
        IndexFileWriter.write(s, new OutputStreamWriter(System.out));
    }
}
