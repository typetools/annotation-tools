package annotations.tests;

import checkers.nullness.quals.*;
import checkers.nullness.quals.NonNull;
import checkers.javari.quals.ReadOnly;

import java.io.*;
import java.util.*;
import java.lang.annotation.RetentionPolicy;

import junit.framework.*;
import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.io.*;
import annotations.util.coll.*;

import plume.FileIOException;

public /*@ReadOnly*/ class TestSceneLib extends TestCase {
    LineNumberReader openPackagedIndexFile(String name) {
        return new LineNumberReader(new InputStreamReader(
                (InputStream) TestSceneLib.class.getResourceAsStream(name)));
    }

    static final String fooIndexContents =
            "package:\n" +
            "annotation @Ready: @Retention(RUNTIME)\n" +
            "annotation @Author: @Retention(CLASS)\n" +
            "String value\n" +
            "class Foo:\n" +
            "field x: @Ready\n" +
            "method y()Z:\n" +
            "parameter #5:\n" +
            "type:\n" +
            "inner-type 1, 2:\n" +
            "@Author(value=\"Matt M.\")\n";

    public static AnnotationDef adAuthor
      = Annotations.createValueAnnotationDef("Author",
                                             Annotations.asRetentionClass,
                                             BasicAFT.forType(String.class));

    static final AnnotationDef ready =
                    new AnnotationDef(
                            "Ready",
                            Annotations.asRetentionRuntime,
                            Annotations.noFieldTypes);
    static final AnnotationDef readyClassRetention =
                    new AnnotationDef(
                            "Ready",
                            Annotations.asRetentionClass,
                            Annotations.noFieldTypes);


    /**
     * Parse indexFileContents as an annotation file, merging the results
     * into s; the final state of s should equal expectScene.
     */
    void doParseTest(String indexFileContents,
                     AScene s,
                     /*@ReadOnly*/ AScene expectScene) {
        try {
            IndexFileParser.parseString(indexFileContents, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (! expectScene.equals(s)) {
          System.err.println("expectScene does not equal s");
          String esu = expectScene.unparse();
          String su = s.unparse();
          if (esu.equals(su)) {
            System.err.println("(but their printed representations are the same)");
          }
          System.err.println(esu);
          System.err.println(su);
        }
        assertEquals(expectScene, s);
    }

    // lazy typist!
    AScene newScene() {
        return new AScene();
    }

    void doParseTest(String index,
                     /*@NonNull*/ /*@ReadOnly*/ AScene expectScene) {
        AScene s = newScene();
        doParseTest(index, s, expectScene);
    }

    private Annotation createEmptyAnnotation(AnnotationDef def) {
        return new Annotation(def, Collections.<String, Object> emptyMap());
    }

    public void testEquals() {
        AScene s1 = newScene(), s2 = newScene();

        s1.classes.vivify("Foo");
        s1.classes.vivify("Foo").fields.vivify("x");
        s1.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(ready));

        s2.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(Annotations.aNonNull);
        s2.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(ready));

        assertEquals(false, s1.equals(s2));

        s1.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(Annotations.aNonNull);

        assertEquals(true, s1.equals(s2));
    }

    public void testStoreParse1() {
        AScene s1 = newScene();

        s1.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(ready));
        Annotation myAuthor = Annotations.createValueAnnotation(adAuthor, "Matt M.");
        s1.classes.vivify("Foo");
        s1.classes.vivify("Foo").methods.vivify("y()Z");
        s1.classes.vivify("Foo").methods.vivify("y()Z").parameters.vivify(5);
        Object dummy = 
          s1.classes.vivify("Foo").methods.vivify("y()Z").parameters.vivify(5).type;
        Object dummy2 = 
          s1.classes.vivify("Foo").methods.vivify("y()Z").parameters.vivify(5).type.innerTypes;
        s1.classes.vivify("Foo").methods.vivify("y()Z").parameters.vivify(5).type.innerTypes
                .vivify(new InnerTypeLocation(Arrays
                                .asList(new Integer[] { 1, 2 }))).tlAnnotationsHere
                .add(myAuthor);

        doParseTest(fooIndexContents, s1);
    }

    private void checkConstructor(AMethod constructor) {
        Annotation ann = ((Annotation) constructor.receiver.lookup("p2.D"));
        assertEquals(Collections.singletonMap("value", "spam"), ann.fieldValues);
        ATypeElement l = (ATypeElement) constructor.locals
                        .get(new LocalLocation(1, 3, 5)).type;
        AElement i = (AElement) l.innerTypes
                        .get(new InnerTypeLocation(
                                Collections.singletonList(0)));
        assertNotNull(i.lookup("p2.C"));
        AElement l2 =
                constructor.locals.get(new LocalLocation(1, 3, 6));
        assertNull(l2);
    }

    public void testParseRetrieve1() throws Exception {
        LineNumberReader fr = openPackagedIndexFile("test1.jann");
        AScene s1 = newScene();
        IndexFileParser.parse(fr, s1);

        AClass foo1 = s1.classes.get("p1.Foo");
        assertNotNull("Didn't find foo1", foo1);
        AClass foo = (AClass) foo1;
        boolean sawConstructor = false;
        for (Map.Entry<String, AMethod> me : foo.methods.entrySet()) {
            if (me.getKey().equals("<init>(Ljava/util/Set;)V")) {
                assertFalse(sawConstructor);
                AMethod constructor = me.getValue();
                assertNotNull(constructor);
                checkConstructor((AMethod) constructor);
                sawConstructor = true;
            }
        }
        assertTrue(sawConstructor);
    }

    class TestDefCollector extends DefCollector {
        AnnotationDef a, b, c, d, e;

        AnnotationDef f;

        public TestDefCollector(AScene s) throws DefException {
            super(s);
        }

        @Override
        protected void visitAnnotationDef(AnnotationDef tldef) {
            if (tldef.name.equals("p2.A")) {
                assertNull(a);
                a = tldef;
            } else if (tldef.name.equals("p2.B")) {
                assertNull(b);
                b = tldef;
            } else if (tldef.name.equals("p2.C")) {
                assertNull(c);
                c = tldef;
            } else if (tldef.name.equals("p2.D")) {
                assertNull(d);
                d = tldef;
            } else if (tldef.name.equals("p2.E")) {
                assertNotNull(f); // should give us fields first
                assertNull(e);
                e = tldef;
            } else if (tldef.name.equals("p2.F")) {
                f = tldef;
            } else {
                fail();
            }
        }
    }

    public void testParseRetrieveTypes() throws Exception {
        LineNumberReader fr = openPackagedIndexFile("test1.jann");
        AScene s1 = newScene();
        IndexFileParser.parse(fr, s1);

        TestDefCollector tdc = new TestDefCollector(s1);
        tdc.visit();
        assertNotNull(tdc.a);
        assertNotNull(tdc.b);
        assertNotNull(tdc.c);
        assertNotNull(tdc.d);
        assertNotNull(tdc.e);
        assertNotNull(tdc.f);

        // now look at p2.E because it has some rather complex types
        AnnotationDef tle = (AnnotationDef) tdc.e;
        assertEquals(RetentionPolicy.CLASS, tle.retention());
        AnnotationDef e = tle;
        assertEquals(new ArrayAFT(new AnnotationAFT(tdc.a)), e.fieldTypes
                .get("first"));
        assertEquals(new AnnotationAFT(tdc.f), e.fieldTypes.get("second"));
        assertEquals(new EnumAFT("Foo"), e.fieldTypes.get("third"));
        assertEquals(
                ClassTokenAFT.ctaft,
                e.fieldTypes.get("fourth"));
        assertEquals(ClassTokenAFT.ctaft, e.fieldTypes.get("fifth"));

        AnnotationDef tla = (AnnotationDef) tdc.a;
        assertEquals(RetentionPolicy.RUNTIME, tla.retention());
        AnnotationDef a = tla;
        assertEquals(BasicAFT.forType(int.class), a.fieldTypes.get("value"));
        AnnotationDef d = tdc.d;
        assertEquals(BasicAFT.forType(String.class), d.fieldTypes.get("value"));
    }

    public void testParseRetrieveValues() throws Exception {
        LineNumberReader fr = openPackagedIndexFile("test1.jann");
        AScene s1 = newScene();
        IndexFileParser.parse(fr, s1);

        // now look at Bar because it has some rather complex values
        Annotation a = s1.classes.get("p1.Bar").lookup("p2.E");

        assertEquals("fooconstant", a.fieldValues.get("third"));
        assertEquals("interface java.util.Map", a.fieldValues.get("fourth").toString());
        assertEquals("class [[I", a.fieldValues.get("fifth").toString());

        List<?> first =
                (List<?>) a.fieldValues.get("first");
        assertEquals(2, first.size(), 2);
        Annotation aa = (Annotation) first.get(0);
        assertEquals("p2.A", aa.def().name);
        assertEquals(-1, aa.fieldValues.get("value"));

        Annotation a2 =
                s1.classes.get("p1.Baz").lookup("p2.E");
        assertEquals("FOO_FOO", a2.fieldValues.get("third"));
        assertEquals("class java.util.LinkedHashMap", a2.fieldValues.get("fourth").toString());
        assertEquals("void", a2.fieldValues.get("fifth").toString());
    }

    void doRewriteTest(LineNumberReader r) throws Exception {
        AScene s1 = newScene(), s2 = newScene();
        IndexFileParser.parse(r, s1);
        StringWriter sbw = new StringWriter();
        IndexFileWriter.write(s1, sbw);
        IndexFileParser.parseString(sbw.toString(), s2);
        assertEquals(s1, s2);
    }

    public void testRewriteOne() throws Exception {
        LineNumberReader fr = openPackagedIndexFile("test1.jann");
        doRewriteTest(fr);
    }

    public void testRewriteTwo() throws Exception {
        LineNumberReader fr = openPackagedIndexFile("test2.jann");
        doRewriteTest(fr);
    }

    public void testConflictedDefinition() throws Exception {
        AScene s1 = newScene();
        s1.classes.vivify("Foo").tlAnnotationsHere
                .add(createEmptyAnnotation(ready));
        s1.classes.vivify("Bar").tlAnnotationsHere
                .add(createEmptyAnnotation(readyClassRetention));
        StringWriter sbw = new StringWriter();
        try {
            IndexFileWriter.write(s1, sbw);
            fail("an exception should have been thrown");
        } catch (DefException de) {
            assertEquals("Ready", de.annotationType);
            // success
        }

    }

    public void testParseErrorMissingColon() throws Exception {
        AScene s1 = newScene();
        String fileContents = "package p1:\n" + "annotation @A:\n"
                              + "class Foo @A";
        try {
            IndexFileParser.parseString(fileContents, s1);
            fail(); // an exception should have been thrown
        } catch (FileIOException e) {
            // TODO:  check line number
            // assertEquals(3, e.line);
            // success
        }
    }

    public void testParseErrorMissingDefinition() throws Exception {
        AScene s1 = newScene();
        String fileContents
          = "package p1:\n" + "annotation @AIsDefined:\n"
          + "class Foo:\n" + "@AIsDefined\n" + "@BIsNotDefined\n";
        try {
            IndexFileParser.parseString(fileContents, s1);
            fail(); // an exception should have been thrown
        } catch (FileIOException e) {
            // TODO: check line number
            // assertEquals(5, e.line);
            // success
        }
    }

    private static Annotation getAnnotation(Set<Annotation> annos, String name) {
        for (Annotation anno : annos) {
            if (anno.def.name.equals(name)) {
                return anno;
            }
        }
        return null;
    }

    private static boolean containsAnnotation(Set<Annotation> annos, String name) {
        return getAnnotation(annos, name) != null;
    }

    private static boolean isReadOnly(AElement e) {
        return e.lookup("ReadOnly") != null;
    }

    // I removed the filtering functionality, for lack of clients that use it. -MDE
    // public void testFilter() throws Exception {
    //     // Suppose we care only about @ReadOnly annotations
    //     AScene s1 = new AScene();
    //     // instead we have to put the definitions in the index file
    //     // s1.annotationDefs.add(ready);
    //     // s1.annotationDefs.add(author);
    //
    //     IndexFileParser.parseString(fooIndexContents, s1);
    //
    //     assertTrue(isReadOnly(s1.classes.get("Foo").fields.get("x")));
    //     // we don't know whether elements on the way to the type argument
    //     // were created, since the author annotation is not wanted
    //     assertFalse(isReadOnly(s1.classes.get("Foo").methods.vivify("y()Z")));
    //     AElement targ =
    //             (AElement) s1.classes
    //                     .get("Foo").methods.vivify("y()Z").parameters.vivify(5).innerTypes
    //                     .vivify(new InnerTypeLocation(Arrays.asList(1, 2)));
    //     // make sure author annotation was skipped
    //     assertTrue(targ.lookup("Author") == null);
    // }

    public void testEmptyArrayHack() throws Exception {
        AScene scene = newScene();
        AClass clazz =
            scene.classes.vivify("bar.Test");

        // One annotation with an empty array of unknown type...
        AnnotationBuilder ab1 =
          AnnotationFactory.saf.beginAnnotation("foo.ArrayAnno", Annotations.asRetentionClass);
        ab1.addEmptyArrayField("array");
        Annotation a1 = ab1.finish();
        Annotation tla1 = a1;

        // ... and another with an empty array of known type
        AnnotationBuilder ab2 =
          AnnotationFactory.saf.beginAnnotation("foo.ArrayAnno", Annotations.asRetentionClass);
        ArrayBuilder ab2ab = ab2.beginArrayField("array",
                new ArrayAFT(BasicAFT.forType(int.class)));
        ab2ab.finish();
        Annotation a2 = ab2.finish();
        Annotation tla2 = a2;

        // And they're both fields of another annotation to make sure that
        // unification works recursively.
        AnnotationBuilder ab3 =
          AnnotationFactory.saf.beginAnnotation("foo.CombinedAnno", Annotations.asRetentionRuntime);
        ab3.addScalarField("fieldOne", new AnnotationAFT(a1.def()), a1);
        ab3.addScalarField("fieldTwo", new AnnotationAFT(a2.def()), a2);
        Annotation a3 = ab3.finish();
        Annotation tla3 = a3;

        clazz.tlAnnotationsHere.add(tla3);

        StringWriter sw = new StringWriter();
        IndexFileWriter.write(scene, sw);

        AScene sceneRead = newScene();
        IndexFileParser.parseString(sw.toString(), sceneRead);

        // the anomaly: see second "consequence" on IndexFileWriter#write
        assertFalse(scene.equals(sceneRead));

        AClass clazz2 = sceneRead.classes.get("bar.Test");
        assertNotNull(clazz2);
        Annotation a3_2 = getAnnotation(clazz2.tlAnnotationsHere, "foo.CombinedAnno");
        Annotation a1_2 = (Annotation) a3_2.getFieldValue("fieldOne");
        Annotation a2_2 = (Annotation) a3_2.getFieldValue("fieldTwo");
        // now that the defs were merged, the annotations should be equal
        assertEquals(a1_2, a2_2);

        // Yet another annotation with an array of a different known type
        AnnotationBuilder ab4 =
          AnnotationFactory.saf.beginAnnotation("foo.ArrayAnno", Annotations.asRetentionClass);
        ArrayBuilder ab4ab = ab4.beginArrayField("array",
                new ArrayAFT(BasicAFT.forType(double.class)));
        ab4ab.appendElement(5.0);
        ab4ab.finish();
        Annotation a4 = ab4.finish();
        Annotation tla4 = a4;

        // try combining unifiable _top-level_ annotations
        AScene secondScene = newScene();
        AClass secondSceneClazz =
            secondScene.classes.vivify("bar.Test");
        secondSceneClazz.tlAnnotationsHere.add(tla1);
        // Oops--the keyed set gives us an exception if we try to put two
        // different foo.ArrayAnnos on the same class!
        AClass secondSceneClazz2 =
            secondScene.classes.vivify("bar.Test2");
        secondSceneClazz2.tlAnnotationsHere.add(tla4);

        // it should be legal to write this
        StringWriter secondSW = new StringWriter();
        IndexFileWriter.write(secondScene, secondSW);

        // add an incompatible annotation
        AClass secondSceneClazz3 =
            secondScene.classes.vivify("bar.Test3");
        secondSceneClazz3.tlAnnotationsHere.add(tla2);

        // now we should get a DefException
        StringWriter secondSceneSW2 = new StringWriter();
        try {
            IndexFileWriter.write(secondScene, secondSceneSW2);
            // we should have gotten an exception
            fail();
        } catch (DefException de) {
            assertEquals("Conflicting definition of annotation type foo.ArrayAnno",
                    de.getMessage());
            // success
        }
    }

    public void testEmptyArrayIO() throws Exception {
        // should succeed
        String index1 = "package: annotation @Foo: @Retention(CLASS)\n  unknown[] arr\n" +
            "class Bar: @Foo(arr={})";
        AScene scene1 = newScene();
        IndexFileParser.parseString(index1, scene1);

        // should reject nonempty array
        String index2 = "package: annotation @Foo:  @Retention(CLASS)\n unknown[] arr\n" +
            "class Bar: @Foo(arr={1})";
        AScene scene2 = newScene();
        try {
            IndexFileParser.parseString(index2, scene2);
            // should have gotten an exception
            fail();
        } catch (FileIOException e) {
            // success
        }

        // construct a scene programmatically
        AScene scene3 = newScene();
        AClass clazz3 =
            scene3.classes.vivify("Bar");
        AnnotationBuilder ab =
          AnnotationFactory.saf.beginAnnotation("Foo", Annotations.asRetentionClass);
        ab.addEmptyArrayField("arr");
        Annotation a = ab.finish();
        Annotation tla = a;
        clazz3.tlAnnotationsHere.add(tla);

        assertEquals(scene1, scene3);

        // when we write the scene out, the index file should contain the
        // special unknown[] field type
        StringWriter sw3 = new StringWriter();
        IndexFileWriter.write(scene3, sw3);
        String index3 = sw3.toString();
        assertTrue(index3.indexOf("unknown[]") >= 0);

        // can we read it back in and get the same thing?
        AScene scene4 = newScene();
        IndexFileParser.parseString(index3, scene4);
        assertEquals(scene3, scene4);
    }

    public void testPrune() {
        AScene s1 = newScene(), s2 = newScene();
        assertTrue(s1.equals(s2));

        s1.classes.vivify("Foo");
        assertFalse(s1.equals(s2));

        assertTrue(s1.prune());
        assertTrue(s1.equals(s2));

        Annotation sa = AnnotationFactory.saf.beginAnnotation("Anno", Annotations.asRetentionClass).finish();
        Annotation tla = sa;

        AClass clazz2 = s2.classes.vivify("Bar");
        clazz2.tlAnnotationsHere.add(tla);

        assertFalse(s1.equals(s2));
        assertFalse(s2.prune());
        assertFalse(s1.equals(s2));
    }

    static class MyTAST {
        final int id;
        MyTAST(int id) {
            this.id = id;
        }

        MyTAST element = null;
        MyTAST[] typeArgs = null;

        static MyTAST arrayOf(int id, MyTAST element) {
            MyTAST t = new MyTAST(id);
            t.element = element;
            return t;
        }

        static MyTAST parameterization(int id, MyTAST... args) {
            MyTAST t = new MyTAST(id);
            t.typeArgs = args;
            return t;
        }
    }

    private static final AnnotationDef idAnnoDef =
        new AnnotationDef("IdAnno", null, Collections.singletonMap(
                "id", BasicAFT.forType(int.class)));
    private static final AnnotationDef idAnnoTLDef =
      new AnnotationDef("IdAnno", Annotations.asRetentionClass,
                          Collections.singletonMap(
                "id", BasicAFT.forType(int.class)));

    static Annotation makeTLIdAnno(int id) {
        return new Annotation(idAnnoTLDef,
                              Collections.singletonMap("id", Integer.valueOf(id)));
    }

    static class MyTASTMapper extends TypeASTMapper<MyTAST> {
        boolean[] saw = new boolean[11];

        @Override
        protected MyTAST getElementType(MyTAST n) {
            return n.element;
        }

        @Override
        protected MyTAST getTypeArgument(MyTAST n, int index) {
            return n.typeArgs[index];
        }

        @Override
        protected int numTypeArguments(MyTAST n) {
            return n.typeArgs == null ? 0 : n.typeArgs.length;
        }

        @Override
        protected void map(MyTAST n, ATypeElement e) {
            int nodeID = n.id;
            if (nodeID == 10) {
                assertTrue(e.lookup("IdAnno") == null);
                e.tlAnnotationsHere.add(makeTLIdAnno(10));
            } else {
                int annoID = (Integer) e.lookup("IdAnno").getFieldValue("id");
                assertEquals(nodeID, annoID);
            }
            assertFalse(saw[nodeID]);
            saw[nodeID] = true;
        }
    }

    private void assignId(ATypeElement myField, int id,
            Integer... ls) {
        AElement el = myField.innerTypes.vivify(
                new InnerTypeLocation(Arrays.asList(ls)));
        el.tlAnnotationsHere.add(makeTLIdAnno(id));
    }

    public void testTypeASTMapper() {
        // Construct a TAST for the type structure:
        // 0< 3<4>[1][2], 5<6, 8[7], 9, 10> >
        MyTAST tast = MyTAST.parameterization(0,
                MyTAST.arrayOf(1,
                        MyTAST.arrayOf(2,
                                MyTAST.parameterization(3,
                                        new MyTAST(4)
                                )
                        )
                ),
                MyTAST.parameterization(5,
                        new MyTAST(6),
                        MyTAST.arrayOf(7,
                                new MyTAST(8)),
                        new MyTAST(9),
                        new MyTAST(10)
                )
        );

        // Pretend myField represents a field of the type represented by tast.
        // We have to do this because clients are no longer allowed to create
        // AElements directly; instead, they must vivify.
        AElement myAField =
            new AScene().classes.vivify("someclass").fields.vivify("somefield");
        ATypeElement myAFieldType = myAField.type;
        // load it with annotations we can check against IDs
        myAFieldType.tlAnnotationsHere.add(makeTLIdAnno(0));
        assignId(myAFieldType, 1, 0);
        assignId(myAFieldType, 2, 0, 0);
        assignId(myAFieldType, 3, 0, 1);
        assignId(myAFieldType, 4, 0, 1, 0);
        assignId(myAFieldType, 5, 1);
        assignId(myAFieldType, 6, 1, 0);
        assignId(myAFieldType, 7, 1, 1);
        assignId(myAFieldType, 8, 1, 1, 0);
        assignId(myAFieldType, 9, 1, 2);
        // to test vivification, we don't assign 10

        // now visit and make sure the ID numbers match up
        MyTASTMapper mapper = new MyTASTMapper();
        mapper.traverse(tast, myAFieldType);

        for (int i = 0; i < 11; i++)
            assertTrue(mapper.saw[i]);
        // make sure it vivified #10 and our annotation stuck
        AElement e10 = myAFieldType.innerTypes.get(
                new InnerTypeLocation(Arrays.asList(1, 3)));
        assertNotNull(e10);
        int e10aid = (Integer) e10.lookup("IdAnno").getFieldValue("id");
        assertEquals(e10aid, 10);
    }
}
