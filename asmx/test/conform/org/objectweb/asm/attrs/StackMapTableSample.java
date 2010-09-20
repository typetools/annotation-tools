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
package org.objectweb.asm.attrs;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to cover StackMapTable variations.
 * 
 * It is compiled using "javac -target 1.6" into StackMapTableSample.data
 * 
 * @author Eugene Kuleshov
 */
public class StackMapTableSample implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static Map MAP;
    static {
        MAP = new HashMap();
        for (int k = 0; k < 10; k++) {
            MAP.put(k < 5 ? "A" + k : "B" + k, new Integer(k));
        }
    }

    private Map map;

    public StackMapTableSample(String s) {
        map = new HashMap();
        for (int k = 0; k < 10; k++) {
            map.put(k < 5 ? "A" + k : "B" + k, new Integer(k));
        }
    }

    public void appendFrame(boolean b) {
        int n = 5;
        if (b)
            return;
    }

    public void appendAndChopFrame(int i) {
        for (int k = 0; k < i; k++) {
        }
    }

    public int sameLocals1stackItemFrame() {
        try {
        } finally {
            return 0;
        }
    }

    public void sameLocals1stackItemFrame2() {
        Object a;
        try {
            a = new Object();
        } catch (Exception e) {
        } finally {
        }
    }

    public int sameLocals1stackItemFrameExtended() {
        try {
            long l10 = 11L;
            long l11 = 11L;
            long l12 = 11L;
            long l13 = 11L;
            long l14 = 11L;
            long l15 = 11L;
            long l16 = 11L;
            long l17 = 11L;
            long l18 = 11L;
            long l19 = 11L;
            long l20 = 11L;
            long l21 = 11L;
            long l22 = 11L; // offset > 64

        } finally {
            return 0;
        }
    }

    public void sameFrameExtended(boolean b) {
        while (true) {
            long l10 = 11L;
            long l11 = 11L;
            long l12 = 11L;
            long l13 = 11L;
            long l14 = 11L;
            long l15 = 11L;
            long l16 = 11L;
            long l17 = 11L;
            long l18 = 11L;
            long l19 = 11L;
            long l20 = 11L;
            long l21 = 11L;
            long l22 = 11L; // offset > 64

            if (b)
                return;
        }
    }

    public void fullFrame(String s) {
        long l10 = 11L;
        long l11 = 11L;
        long l12 = 11L;
        long l13 = 11L;

        if (s == null)
            return;
    }

}
