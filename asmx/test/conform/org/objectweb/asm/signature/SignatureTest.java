/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.signature;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Signature tests.
 * 
 * @author Thomas Hallgren
 * @author Eric Bruneton
 */
public class SignatureTest extends TestCase {

    private String line;

    public SignatureTest(String line) {
        super("test");
        this.line = line;
    }

    public static TestSuite suite() throws Exception {
        TestSuite suite = new TestSuite();
        InputStream is = SignatureTest.class.getResourceAsStream("signatures.txt");
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));

        String line;
        while ((line = lnr.readLine()) != null) {
            if (line.length() < 2) {
                continue;
            }
            suite.addTest(new SignatureTest(line));
        }
        lnr.close();
        return suite;
    }

    public void test() throws Exception {
        if (line.length() > 2) {
            String signature = line.substring(2);
            SignatureWriter wrt = new SignatureWriter();
            SignatureReader rdr = new SignatureReader(signature);
            switch (line.charAt(0)) {
                case 'C':
                case 'M':
                    rdr.accept(wrt);
                    break;
                case 'T':
                    rdr.acceptType(wrt);
                    break;
                default:
                    return;
            }
            assertEquals(signature, wrt.toString());
        }
    }
}
