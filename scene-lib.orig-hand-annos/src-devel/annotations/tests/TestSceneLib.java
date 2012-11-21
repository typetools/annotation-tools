package annotations.tests;

import java.io.*;
import java.util.*;

import junit.framework.*;
import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.io.*;
import annotations.util.coll.*;

public /*@Unmodifiable*/ class TestSceneLib extends TestCase {
    /*@NonNull*/ Reader openPackagedIndexFile(/*@NonNull*/ String name) {
        return new InputStreamReader(
                (/*@NonNull*/ InputStream) TestSceneLib.class
                        .getResourceAsStream(name));
    }

    static final /*@NonNull*/ String fooIndex =
            "package:\n" + "annotation visible @ReadOnly:\n"
                    + "annotation invisible @Author:\n" + "String value\n"
                    + "class Foo:\n" + "field x: @ReadOnly\n" + "method y()Z:\n"
                    + "parameter #5:\n" + "inner-type 1, 2:\n"
                    + "@Author(value=\"Matt M.\")\n";

    void doParseTest(/*@NonNull*/ String index, /*@NonNull*/
    AScene<SimpleAnnotation> s,
    /*@NonNull*/ /*@ReadOnly*/ AScene<SimpleAnnotation> expectScene) {
        try {
            IndexFileParser.parse(new StringReader(index), s);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(expectScene, s);
    }

    // lazy typist!
    /*@NonNull*/ AScene<SimpleAnnotation> newScene() {
        return new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf);
    }

    void doParseTest(/*@NonNull*/ String index, /*@NonNull*/
    /*@ReadOnly*/ AScene<SimpleAnnotation> expectScene) {
        /*@NonNull*/ AScene<SimpleAnnotation> s = newScene();
        doParseTest(index, s, expectScene);
    }

    // just for fun
    static final /*@NonNull*/ TLAnnotationDef readOnly =
            new TLAnnotationDef(
                    new AnnotationDef(
                            "ReadOnly",
                            Collections
                                    .</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType> emptyMap()),
                    RetentionPolicy.RUNTIME);

    static final /*@NonNull*/ TLAnnotationDef nonnull =
            new TLAnnotationDef(
                    new AnnotationDef(
                            "Nonnull",
                            Collections
                                    .</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType> emptyMap()),
                    RetentionPolicy.RUNTIME);

    static final /*@NonNull*/ TLAnnotationDef author =
            new TLAnnotationDef(new AnnotationDef("Author", Collections
                    .<String, AnnotationFieldType> singletonMap("value",
                            BasicAFT.forType(String.class))),
                    RetentionPolicy.CLASS);

    private /*@NonNull*/ TLAnnotation<SimpleAnnotation> createEmptyAnnotation(
    /*@NonNull*/ TLAnnotationDef def) {
        return new TLAnnotation<SimpleAnnotation>(def, new SimpleAnnotation(
                def.def, Collections
                        .</*@NonNull*/ String, /*@NonNull*/ Object> emptyMap()));
    }

    public void testEquals() {
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene(), s2 = newScene();

        s1.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(readOnly));

        s2.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(nonnull));
        s2.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(readOnly));

        assertEquals(false, s1.equals(s2));

        s1.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(nonnull));

        assertEquals(true, s1.equals(s2));
    }

    public void testStoreParse1() {
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();

        s1.classes.vivify("Foo").fields.vivify("x").tlAnnotationsHere
                .add(createEmptyAnnotation(readOnly));
        /*@NonNull*/ Map</*@NonNull*/ String, /*@NonNull*/ Object> myfields =
                new LinkedHashMap</*@NonNull*/ String, /*@NonNull*/ Object>();
        myfields.put("value", "Matt M.");
        /*@NonNull*/ SimpleAnnotation myauthor =
                new SimpleAnnotation(author.def, myfields);
        /*@NonNull*/ TLAnnotation<SimpleAnnotation> tlMyAuthor =
                new TLAnnotation<SimpleAnnotation>(author, myauthor);
        s1.classes.vivify("Foo").methods.vivify("y()Z").parameters.vivify(5).innerTypes
                .vivify(new InnerTypeLocation(Arrays
                                .asList(new /*@NonNull*/ Integer[] { 1, 2 }))).tlAnnotationsHere
                .add(tlMyAuthor);

        doParseTest(fooIndex, s1);
    }

    private void checkConstructor(
    /*@NonNull*/ AMethod<SimpleAnnotation> constructor) {
        /*@NonNull*/ SimpleAnnotation ann =
                ((/*@NonNull*/ TLAnnotation<SimpleAnnotation>) constructor.receiver.tlAnnotationsHere
                        .lookup("p2.D")).ann;
        assertEquals(Collections.singletonMap("value", "spam"), ann.fieldValues);
        /*@NonNull*/ ATypeElement<SimpleAnnotation> l =
                (/*@NonNull*/ ATypeElement<SimpleAnnotation>) constructor.locals
                        .get(new LocalLocation(1, 3, 5));
        /*@NonNull*/ AElement<SimpleAnnotation> i =
                (/*@NonNull*/ AElement<SimpleAnnotation>) l.innerTypes
                        .get(new InnerTypeLocation(
                                Collections.singletonList(0)));
        assertNotNull(i.tlAnnotationsHere.lookup("p2.C"));
        ATypeElement<SimpleAnnotation> l2 =
                constructor.locals.get(new LocalLocation(1, 3, 6));
        assertNull(l2);
    }

    public void testParseRetrieve1() throws Exception {
        /*@NonNull*/ Reader fr = openPackagedIndexFile("test1.jann");
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();
        IndexFileParser.parse(fr, s1);

        AClass<SimpleAnnotation> foo1 = s1.classes.get("p1.Foo");
        assertNotNull(foo1);
        /*@NonNull*/ AClass<SimpleAnnotation> foo =
                (/*@NonNull*/ AClass<SimpleAnnotation>) foo1;
        boolean sawConstructor = false;
        for (/*@NonNull*/ Map.Entry</*@NonNull*/ String, /*@NonNull*/ AMethod<SimpleAnnotation>> me : foo.methods
                .entrySet()) {
            if (me.getKey().equals("<init>(Ljava/util/Set;)V")) {
                assertFalse(sawConstructor);
                AMethod<SimpleAnnotation> constructor = me.getValue();
                assertNotNull(constructor);
                checkConstructor((/*@NonNull*/ AMethod<SimpleAnnotation>) constructor);
                sawConstructor = true;
            }
        }
        assertTrue(sawConstructor);
    }

    class TestDefCollector extends DefCollector {
        TLAnnotationDef a, b, c, d, e;

        AnnotationDef f;

        public TestDefCollector(/*@NonNull*/ AScene<?> s) throws DefException {
            super(s);
        }

        @Override
        protected void visitAnnotationDef(/*@NonNull*/ AnnotationDef def) {
            if (def.name.equals("p2.F"))
                f = def;
            else
                fail();
        }

        @Override
        protected void visitTLAnnotationDef(/*@NonNull*/ TLAnnotationDef tldef) {
            if (tldef.def.name.equals("p2.A")) {
                assertNull(a);
                a = tldef;
            } else if (tldef.def.name.equals("p2.B")) {
                assertNull(b);
                b = tldef;
            } else if (tldef.def.name.equals("p2.C")) {
                assertNull(c);
                c = tldef;
            } else if (tldef.def.name.equals("p2.D")) {
                assertNull(d);
                d = tldef;
            } else if (tldef.def.name.equals("p2.E")) {
                assertNotNull(f); // should give us fields first
                assertNull(e);
                e = tldef;
            } else
                fail();
        }
    }

    public void testParseRetrieveTypes() throws Exception {
        /*@NonNull*/ Reader fr = openPackagedIndexFile("test1.jann");
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();
        IndexFileParser.parse(fr, s1);

        /*@NonNull*/ TestDefCollector tdc = new TestDefCollector(s1);
        tdc.visit();
        assertNotNull(tdc.a);
        assertNotNull(tdc.b);
        assertNotNull(tdc.c);
        assertNotNull(tdc.d);
        assertNotNull(tdc.e);
        assertNotNull(tdc.f);

        // now look at p2.E because it has some rather complex types
        /*@NonNull*/ TLAnnotationDef tle = (/*@NonNull*/ TLAnnotationDef) tdc.e;
        assertEquals(RetentionPolicy.CLASS, tle.retention);
        /*@NonNull*/ AnnotationDef e = tle.def;
        assertEquals(new ArrayAFT(new AnnotationAFT(tdc.a.def)), e.fieldTypes
                .get("first"));
        assertEquals(new AnnotationAFT(tdc.f), e.fieldTypes.get("second"));
        assertEquals(new EnumAFT("Foo"), e.fieldTypes.get("third"));
        assertEquals(
                ClassTokenAFT.ctaft,
                e.fieldTypes.get("fourth"));
        assertEquals(ClassTokenAFT.ctaft, e.fieldTypes.get("fifth"));

        /*@NonNull*/ TLAnnotationDef tla = (/*@NonNull*/ TLAnnotationDef) tdc.a;
        assertEquals(RetentionPolicy.RUNTIME, tla.retention);
        /*@NonNull*/ AnnotationDef a = tla.def;
        assertEquals(BasicAFT.forType(int.class), a.fieldTypes.get("value"));
        /*@NonNull*/ AnnotationDef d =
                ((/*@NonNull*/ TLAnnotationDef) tdc.d).def;
        assertEquals(BasicAFT.forType(String.class), d.fieldTypes.get("value"));
    }

    public void testParseRetrieveValues() throws Exception {
        /*@NonNull*/ Reader fr = openPackagedIndexFile("test1.jann");
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();
        IndexFileParser.parse(fr, s1);

        // now look at Bar because it has some rather complex values
        /*@NonNull*/ KeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotation<SimpleAnnotation>> anns =
                s1.classes.get("p1.Bar").tlAnnotationsHere;
        SimpleAnnotation a = anns.lookup("p2.E").ann;

        assertEquals("fooconstant", a.fieldValues.get("third"));
        assertEquals("java.util.Map", a.fieldValues.get("fourth"));
        assertEquals("[[I", a.fieldValues.get("fifth"));

        /*@NonNull*/ List<?> first =
                (/*@NonNull*/ List<?>) a.fieldValues.get("first");
        assertEquals(2, first.size(), 2);
        /*@NonNull*/ SimpleAnnotation aa =
                (/*@NonNull*/ SimpleAnnotation) first.get(0);
        assertEquals("p2.A", aa.def().name);
        assertEquals(-1, aa.fieldValues.get("value"));

        /*@NonNull*/ SimpleAnnotation a2 =
                s1.classes.get("p1.Baz").tlAnnotationsHere.lookup("p2.E").ann;
        assertEquals("FOO_FOO", a2.fieldValues.get("third"));
        assertEquals("java.util.LinkedHashMap", a2.fieldValues.get("fourth"));
        assertEquals("void", a2.fieldValues.get("fifth"));
    }

    void doRewriteTest(/*@NonNull*/ Reader r) throws Exception {
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene(), s2 = newScene();
        IndexFileParser.parse(r, s1);
        /*@NonNull*/ StringWriter sbw = new StringWriter();
        IndexFileWriter.write(s1, sbw);
        /*@NonNull*/ StringReader sr = new StringReader(sbw.toString());
        IndexFileParser.parse(sr, s2);
        assertEquals(s1, s2);
    }

    public void testRewriteOne() throws Exception {
        /*@NonNull*/ Reader fr = openPackagedIndexFile("test1.jann");
        doRewriteTest(fr);
    }

    public void testRewriteTwo() throws Exception {
        /*@NonNull*/ Reader fr = openPackagedIndexFile("test2.jann");
        doRewriteTest(fr);
    }

    public void testConflictedDefinition() throws Exception {
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();
        s1.classes.vivify("Foo").tlAnnotationsHere
                .add(createEmptyAnnotation(readOnly));
        TLAnnotationDef badReadOnly =
                new TLAnnotationDef(readOnly.def, RetentionPolicy.CLASS);
        s1.classes.vivify("Bar").tlAnnotationsHere
                .add(createEmptyAnnotation(badReadOnly));
        /*@NonNull*/ StringWriter sbw = new StringWriter();
        try {
            IndexFileWriter.write(s1, sbw);
            fail(); // an exception should have been thrown
        } catch (DefException de) {
            assertEquals("ReadOnly", de.annotationType);
            // success
        }
    }

    public void testParseErrorMissingColon() throws Exception {
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();
        /*@NonNull*/ StringReader in =
                new StringReader("package p1:\n" + "annotation @A:\n"
                        + "class Foo @A");
        try {
            IndexFileParser.parse(in, s1);
            fail(); // an exception should have been thrown
        } catch (ParseException e) {
            assertEquals(3, e.line);
            // success
        }
    }

    public void testParseErrorMissingDefinition() throws Exception {
        /*@NonNull*/ AScene<SimpleAnnotation> s1 = newScene();
        /*@NonNull*/ StringReader in =
                new StringReader("package p1:\n" + "annotation @A:\n"
                        + "class Foo:\n" + "@A\n" + "@B\n");
        try {
            IndexFileParser.parse(in, s1);
            fail(); // an exception should have been thrown
        } catch (ParseException e) {
            assertEquals(5, e.line);
            // success
        }
    }

    class ReadOnlyAnnotation extends AbstractAnnotation {
        public ReadOnlyAnnotation() {
            super(readOnly.def);
        }

        public Object getFieldValue(/*@NonNull*/ String fieldName) {
            return null; // no fields
        }
    }

    class ReadOnlyAnnotationBuilder implements
            AnnotationBuilder<ReadOnlyAnnotation> {

        public void addScalarField(/*@NonNull*/ String fieldName, /*@NonNull*/
        ScalarAFT aft, /*@NonNull*/ Object x) {
            throw new UnsupportedOperationException();
        }

        public /*@NonNull*/ ArrayBuilder beginArrayField(
        /*@NonNull*/ String fieldName, /*@NonNull*/ ArrayAFT aft) {
            throw new UnsupportedOperationException();
        }

        public void addEmptyArrayField(/*@NonNull*/ String fieldName) {
            throw new UnsupportedOperationException();
        }

        public /*@NonNull*/ ReadOnlyAnnotation finish() {
            return new ReadOnlyAnnotation();
        }
    }

    class ReadOnlyAnnotationFactory implements
            AnnotationFactory<ReadOnlyAnnotation> {
        public ReadOnlyAnnotationBuilder beginAnnotation(
        /*@NonNull*/ String typeName) {
            if (typeName.equals("ReadOnly"))
                return new ReadOnlyAnnotationBuilder();
            else
                return null;
        }
    }

    private boolean isReadOnly(/*@NonNull*/ AElement<ReadOnlyAnnotation> e) {
        return e.tlAnnotationsHere.lookup("ReadOnly") != null;
    }

    public void testFilter() throws Exception {
        // Suppose we care only about @ReadOnly annotations
        /*@NonNull*/ AScene<ReadOnlyAnnotation> s1 =
                new AScene<ReadOnlyAnnotation>(new ReadOnlyAnnotationFactory());
        // instead we have to put the definitions in the index file
        // s1.annotationDefs.add(readOnly);
        // s1.annotationDefs.add(author);

        /*@NonNull*/ StringReader in = new StringReader(fooIndex);

        IndexFileParser.parse(in, s1);

        assertTrue(isReadOnly(s1.classes.get("Foo").fields.get("x")));
        // we don't know whether elements on the way to the type argument
        // were created, since the author annotation is not wanted
        assertFalse(isReadOnly(s1.classes.get("Foo").methods.vivify("y()Z")));
        /*@NonNull*/ AElement<ReadOnlyAnnotation> targ =
                (/*@NonNull*/ AElement<ReadOnlyAnnotation>) s1.classes
                        .get("Foo").methods.vivify("y()Z").parameters.vivify(5).innerTypes
                        .vivify(new InnerTypeLocation(Arrays.asList(1, 2)));
        // make sure author annotation was skipped
        assertNull(targ.tlAnnotationsHere.lookup("Author"));
    }

    public void testEmptyArrayHack() throws Exception {
        /*@NonNull*/ AScene<SimpleAnnotation> scene = newScene();
        /*@NonNull*/ AClass<SimpleAnnotation> clazz =
            scene.classes.vivify("bar.Test");
        
        // One annotation with an empty array of unknown type...
        AnnotationBuilder<SimpleAnnotation> ab1 =
            scene.af.beginAnnotation("foo.ArrayAnno");
        ab1.addEmptyArrayField("array");
        SimpleAnnotation a1 = ab1.finish();
        TLAnnotation<SimpleAnnotation> tla1 =
            new TLAnnotation<SimpleAnnotation>(a1, RetentionPolicy.CLASS);
        
        // ... and another with an empty array of known type
        AnnotationBuilder<SimpleAnnotation> ab2 =
            scene.af.beginAnnotation("foo.ArrayAnno");
        ArrayBuilder ab2ab = ab2.beginArrayField("array",
                new ArrayAFT(BasicAFT.forType(int.class)));
        ab2ab.finish();
        SimpleAnnotation a2 = ab2.finish();
        TLAnnotation<SimpleAnnotation> tla2 =
            new TLAnnotation<SimpleAnnotation>(a2, RetentionPolicy.CLASS);
        
        // And they're both fields of another annotation to make sure that
        // unification works recursively.
        AnnotationBuilder<SimpleAnnotation> ab3 =
            scene.af.beginAnnotation("foo.CombinedAnno");
        ab3.addScalarField("fieldOne", new AnnotationAFT(a1.def()), a1);
        ab3.addScalarField("fieldTwo", new AnnotationAFT(a2.def()), a2);
        SimpleAnnotation a3 = ab3.finish();
        TLAnnotation<SimpleAnnotation> tla3 =
            new TLAnnotation<SimpleAnnotation>(a3, RetentionPolicy.RUNTIME);
        
        clazz.tlAnnotationsHere.add(tla3);
        
        StringWriter sw = new StringWriter();
        IndexFileWriter.write(scene, sw);
        
        /*@NonNull*/ AScene<SimpleAnnotation> sceneRead = newScene();
        StringReader sr = new StringReader(sw.toString());
        IndexFileParser.parse(sr, sceneRead);
        
        // the anomaly: see second "consequence" on IndexFileWriter#write
        assertFalse(scene.equals(sceneRead));
        
        AClass<SimpleAnnotation> clazz2 = sceneRead.classes.get("bar.Test");
        assertNotNull(clazz2);
        SimpleAnnotation a3_2 = clazz2.lookupAnnotation("foo.CombinedAnno");
        SimpleAnnotation a1_2 = (SimpleAnnotation) a3_2.getFieldValue("fieldOne");
        SimpleAnnotation a2_2 = (SimpleAnnotation) a3_2.getFieldValue("fieldTwo");
        // now that the defs were merged, the annotations should be equal
        assertEquals(a1_2, a2_2);
        
        // Yet another annotation with an array of a different known type
        AnnotationBuilder<SimpleAnnotation> ab4 =
            scene.af.beginAnnotation("foo.ArrayAnno");
        ArrayBuilder ab4ab = ab4.beginArrayField("array",
                new ArrayAFT(BasicAFT.forType(double.class)));
        ab4ab.appendElement(5.0);
        ab4ab.finish();
        SimpleAnnotation a4 = ab4.finish();
        TLAnnotation<SimpleAnnotation> tla4 =
            new TLAnnotation<SimpleAnnotation>(a4, RetentionPolicy.CLASS);
        
        // try combining unifiable _top-level_ annotations
        /*@NonNull*/ AScene<SimpleAnnotation> secondScene = newScene();
        /*@NonNull*/ AClass<SimpleAnnotation> secondSceneClazz =
            secondScene.classes.vivify("bar.Test");
        secondSceneClazz.tlAnnotationsHere.add(tla1);
        // Oops--the keyed set gives us an exception if we try to put two
        // different foo.ArrayAnnos on the same class!
        /*@NonNull*/ AClass<SimpleAnnotation> secondSceneClazz2 =
            secondScene.classes.vivify("bar.Test2");
        secondSceneClazz2.tlAnnotationsHere.add(tla4);
        
        // it should be legal to write this
        StringWriter secondSW = new StringWriter();
        IndexFileWriter.write(secondScene, secondSW);
        
        // add an incompatible annotation
        /*@NonNull*/ AClass<SimpleAnnotation> secondSceneClazz3 =
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
        String index1 = "package: annotation invisible @Foo: unknown[] arr\n" +
            "class Bar: @Foo(arr={})";
        /*@NonNull*/ AScene<SimpleAnnotation> scene1 = newScene();
        IndexFileParser.parse(new StringReader(index1), scene1);
        
        // should reject nonempty array
        String index2 = "package: annotation invisible @Foo: unknown[] arr\n" +
            "class Bar: @Foo(arr={1})";
        /*@NonNull*/ AScene<SimpleAnnotation> scene2 = newScene();
        try {
            IndexFileParser.parse(new StringReader(index2), scene2);
            // should have gotten an exception
            fail();
        } catch (ParseException e) {
            // success
        }
        
        // construct a scene programmatically
        /*@NonNull*/ AScene<SimpleAnnotation> scene3 = newScene();
        /*@NonNull*/ AClass<SimpleAnnotation> clazz3 =
            scene3.classes.vivify("Bar");
        AnnotationBuilder<SimpleAnnotation> ab =
            scene3.af.beginAnnotation("Foo");
        ab.addEmptyArrayField("arr");
        SimpleAnnotation a = ab.finish();
        TLAnnotation<SimpleAnnotation> tla =
            new TLAnnotation<SimpleAnnotation>(a, RetentionPolicy.CLASS);
        clazz3.tlAnnotationsHere.add(tla);
        
        assertEquals(scene1, scene3);
        
        // when we write the scene out, the index file should contain the
        // special unknown[] field type
        StringWriter sw3 = new StringWriter();
        IndexFileWriter.write(scene3, sw3);
        String index3 = sw3.toString();
        assertTrue(index3.indexOf("unknown[]") >= 0);
        
        // can we read it back in and get the same thing?
        /*@NonNull*/ AScene<SimpleAnnotation> scene4 = newScene();
        IndexFileParser.parse(new StringReader(index3), scene4);
        assertEquals(scene3, scene4);
    }
    
    public void testPrune() {
        AScene<SimpleAnnotation> s1 = newScene(), s2 = newScene();
        assertTrue(s1.equals(s2));
        
        s1.classes.vivify("Foo");
        assertFalse(s1.equals(s2));
        
        assertTrue(s1.prune());
        assertTrue(s1.equals(s2));
        
        SimpleAnnotation sa = s1.af.beginAnnotation("Anno").finish();
        TLAnnotation<SimpleAnnotation> tla =
            new TLAnnotation<SimpleAnnotation>(sa, RetentionPolicy.CLASS);
        
        AClass<SimpleAnnotation> clazz2 = s2.classes.vivify("Bar");
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
        new AnnotationDef("IdAnno", Collections.singletonMap(
                "id", BasicAFT.forType(int.class)));
    private static final TLAnnotationDef idAnnoTLDef =
        new TLAnnotationDef(idAnnoDef, RetentionPolicy.CLASS);
    
    static TLAnnotation<SimpleAnnotation> makeTLIdAnno(int id) {
        return new TLAnnotation<SimpleAnnotation>(idAnnoTLDef,
                new SimpleAnnotation(idAnnoDef,
                        Collections.singletonMap("id", Integer.valueOf(id))
                )
        );
    }
    
    static class MyTASTMapper extends TypeASTMapper<SimpleAnnotation, MyTAST> {
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
        protected void map(MyTAST n, AElement<SimpleAnnotation> e) {
            int nodeID = n.id;
            if (nodeID == 10) {
                assertNull(e.lookupAnnotation("IdAnno"));
                e.tlAnnotationsHere.add(makeTLIdAnno(10));
            } else {
                int annoID = (Integer) e.lookupAnnotation("IdAnno")
                    .getFieldValue("id");
                assertEquals(nodeID, annoID);
            }
            assertFalse(saw[nodeID]);
            saw[nodeID] = true;
        }
    }
    
    private void assignId(ATypeElement<SimpleAnnotation> myField, int id,
            Integer... ls) {
        AElement<SimpleAnnotation> el = myField.innerTypes.vivify(
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

        // Pretend myField represents a field of the type represented by tast
        // We have to do this because clients are no longer allowed to create
        // AElements directly; instead, they must vivify.
        ATypeElement<SimpleAnnotation> myAField =
            new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf)
                .classes.vivify("someclass").fields.vivify("somefield");
        // load it with annotations we can check against IDs
        myAField.tlAnnotationsHere.add(makeTLIdAnno(0));
        assignId(myAField, 1, 0, 0);
        assignId(myAField, 2, 0, 1);
        assignId(myAField, 3, 0);
        assignId(myAField, 4, 0, 0, 0);
        assignId(myAField, 5, 1);
        assignId(myAField, 6, 1, 0);
        assignId(myAField, 7, 1, 1, 0);
        assignId(myAField, 8, 1, 1);
        assignId(myAField, 9, 1, 2);
        // to test vivification, we don't assign 10
        
        // now visit and make sure the ID numbers match up
        MyTASTMapper mapper = new MyTASTMapper();
        mapper.traverse(tast, myAField);
        
        for (int i = 0; i < 11; i++)
            assertTrue(mapper.saw[i]);
        // make sure it vivified #10 and our annotation stuck
        AElement<SimpleAnnotation> e10 = myAField.innerTypes.get(
                new InnerTypeLocation(Arrays.asList(1, 3)));
        assertNotNull(e10);
        int e10aid = (Integer) e10.lookupAnnotation("IdAnno")
            .getFieldValue("id");
        assertEquals(e10aid, 10);
    }
}
