package org.objectweb.asm.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import junit.framework.TestCase;

public class UnitTest extends TestCase implements Opcodes {

    public void testNodes() {
        InsnNode in = InsnNode.getByOpcode(NOP);
        assertEquals(in.getOpcode(), NOP);
        assertEquals(in.getType(), AbstractInsnNode.INSN);

        IntInsnNode iin = new IntInsnNode(BIPUSH, 0);
        iin.setOpcode(SIPUSH);
        assertEquals(iin.getOpcode(), SIPUSH);
        assertEquals(iin.getType(), AbstractInsnNode.INT_INSN);

        VarInsnNode vn = new VarInsnNode(ALOAD, 0);
        vn.setOpcode(ASTORE);
        assertEquals(vn.getOpcode(), ASTORE);
        assertEquals(vn.getType(), AbstractInsnNode.VAR_INSN);

        TypeInsnNode tin = new TypeInsnNode(NEW, "java/lang/Object");
        tin.setOpcode(CHECKCAST);
        assertEquals(tin.getOpcode(), CHECKCAST);
        assertEquals(tin.getType(), AbstractInsnNode.TYPE_INSN);

        FieldInsnNode fn = new FieldInsnNode(GETSTATIC, "owner", "name", "I");
        fn.setOpcode(PUTSTATIC);
        assertEquals(fn.getOpcode(), PUTSTATIC);
        assertEquals(fn.getType(), AbstractInsnNode.FIELD_INSN);

        MethodInsnNode mn = new MethodInsnNode(INVOKESTATIC,
                "owner",
                "name",
                "I");
        mn.setOpcode(INVOKESPECIAL);
        assertEquals(mn.getOpcode(), INVOKESPECIAL);
        assertEquals(mn.getType(), AbstractInsnNode.METHOD_INSN);

        JumpInsnNode jn = new JumpInsnNode(GOTO, new Label());
        jn.setOpcode(IFEQ);
        assertEquals(jn.getOpcode(), IFEQ);
        assertEquals(jn.getType(), AbstractInsnNode.JUMP_INSN);

        LabelNode ln = new LabelNode(new Label());
        assertEquals(ln.getType(), AbstractInsnNode.LABEL);

        IincInsnNode iincn = new IincInsnNode(1, 1);
        assertEquals(iincn.getType(), AbstractInsnNode.IINC_INSN);

        LdcInsnNode ldcn = new LdcInsnNode("s");
        assertEquals(ldcn.getType(), AbstractInsnNode.LDC_INSN);

        LookupSwitchInsnNode lsn = new LookupSwitchInsnNode(null, null, null);
        assertEquals(lsn.getType(), AbstractInsnNode.LOOKUPSWITCH_INSN);

        TableSwitchInsnNode tsn = new TableSwitchInsnNode(0, 1, null, null);
        assertEquals(tsn.getType(), AbstractInsnNode.TABLESWITCH_INSN);

        MultiANewArrayInsnNode manan = new MultiANewArrayInsnNode("[[I", 2);
        assertEquals(manan.getType(), AbstractInsnNode.MULTIANEWARRAY_INSN);
    }

}
