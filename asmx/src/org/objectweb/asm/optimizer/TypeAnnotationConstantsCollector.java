/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
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
package org.objectweb.asm.optimizer;

import org.objectweb.asm.TypeAnnotationVisitor;

/**
 * An {@link TypeAnnotationVisitor} that collects the 
 * {@link Constant}s of the extended annotations it visits.
 * 
 * @author jaimeq
 */
public class TypeAnnotationConstantsCollector 
  extends AnnotationConstantsCollector 
  implements TypeAnnotationVisitor {

    private TypeAnnotationVisitor xav;

    public TypeAnnotationConstantsCollector(
        final TypeAnnotationVisitor xav,
        final ConstantPool cp)
    {
        super(xav, cp);
        this.xav = xav;
    }

    public void visitXTargetType(int target_type) {
        xav.visitXTargetType(target_type);
    }

    public void visitXOffset(int offset) {
        xav.visitXOffset(offset);
    }

    public void visitXLocationLength(int location_length) {
        xav.visitXLocationLength(location_length);
    }

    public void visitXLocation(int location) {
        xav.visitXLocation(location);
    }

    public void visitXNumEntries(int num_entries) {
        xav.visitXNumEntries(num_entries);
    }

    public void visitXStartPc(int start_pc) {
        xav.visitXStartPc(start_pc);
    }

    public void visitXLength(int length) {
        xav.visitXLength(length);
    }

    public void visitXIndex(int index) {
        xav.visitXIndex(index);
    }

    public void visitXParamIndex(int param_index) {
        xav.visitXParamIndex(param_index);
    }

    public void visitXBoundIndex(int bound_index) {
        xav.visitXBoundIndex(bound_index);
    }

    public void visitXTypeIndex(int type_index) {
        xav.visitXTypeIndex(type_index);
    }
}
