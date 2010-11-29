package annotations.io;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_NUMBER;
import static java.io.StreamTokenizer.TT_WORD;

import java.io.*;
import java.util.*;

import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.util.coll.*;

/**
 * <code>IndexFileParser</code> provides a static method that parses a given
 * index file (provided as a {@link Reader}) into a given {@link AScene}.
 */
public final class IndexFileParser<A extends Annotation> {
    private final /*@NonNull*/ AScene<A> scene;

    private final /*@NonNull*/ StreamTokenizer st;

    private /*@NonNull*/ String curPkgPrefix;

    /** Holds definitions we've seen so far */
    private final /*@NonNull*/ KeyedSet</*@NonNull*/ String, /*@NonNull*/ AnnotationDef> defs;
    
    /** Holds top-level definitions we've seen so far */
    private final /*@NonNull*/ KeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef> tldefs;
    
    private void expect(boolean b, /*@NonNull*/ String msg)
            throws ParseException {
        if (!b)
            throw new ParseException(st.lineno(), msg);
    }

    private int expectNN(int i) throws ParseException {
        if (i >= 0)
            return i;
        else
            throw new ParseException(st.lineno(),
                    "Expected a nonnegative integer");
    }

    private /*@NonNull*/ String expect(String s, /*@NonNull*/ String msg)
            throws ParseException {
        if (s == null)
            throw new ParseException(st.lineno(), msg);
        else
            return (/*@NonNull*/ String) s;
    }

    private boolean checkChar(char c) /*@ReadOnly*/ {
        return st.ttype == c;
    }

    private boolean matchChar(char c) throws IOException {
        if (checkChar(c)) {
            st.nextToken();
            return true;
        } else
            return false;
    }

    private void expectChar(char c) throws IOException, ParseException {
        expect(matchChar(c), "Expected `" + c + "'");
    }

    private boolean checkKeyword(/*@NonNull*/ String s) /*@ReadOnly*/ {
        return st.ttype == TT_WORD && st.sval.equals(s);
    }

    private boolean matchKeyword(/*@NonNull*/ String s) throws IOException {
        if (checkKeyword(s)) {
            st.nextToken();
            return true;
        } else
            return false;
    }

    private void expectKeyword(/*@NonNull*/ String s) throws IOException,
            ParseException {
        expect(matchKeyword(s), "Expected `" + s + "'");
    }

    private static final /*@NonNull*/ /*@ReadOnly*/ Set</*@NonNull*/ String>
        knownKeywords;
    static {
        /*@NonNull*/ /*@ReadOnly*/ String[/*@NonNull*/] knownKeywords1 =
                { "abstract", "assert", "boolean", "break", "byte", "case",
                        "catch", "char", "class", "const", "continue",
                        "default", "do", "double", "else", "enum", "extends",
                        "false", "final", "finally", "float", "for", "if",
                        "goto", "implements", "import", "instanceof", "int",
                        "interface", "long", "native", "new", "null",
                        "package", "private", "protected", "public", "return",
                        "short", "static", "strictfp", "super", "switch",
                        "synchronized", "this", "throw", "throws", "transient",
                        "true", "try", "void", "volatile", "while", };
        /*@NonNull*/  Set</*@NonNull*/ String> knownKeywords2
            = new LinkedHashSet</*@NonNull*/ String>();
        Collections.addAll(knownKeywords2, knownKeywords1);
        knownKeywords = knownKeywords2;
    }

    private boolean isValidIdentifier(/*@NonNull*/ String x) /*@ReadOnly*/ {
        if (x.length() == 0 || !Character.isJavaIdentifierStart(x.charAt(0))
                || knownKeywords.contains(x))
            return false;
        for (int i = 1; i < x.length(); i++)
            if (!Character.isJavaIdentifierPart(x.charAt(i)))
                return false;
        return true;
    }

    private String checkIdentifier() /*@ReadOnly*/ {
        if (st.ttype == TT_WORD && isValidIdentifier(st.sval))
            return st.sval;
        else
            return null;
    }

    private String matchIdentifier() throws IOException {
        String x = checkIdentifier();
        if (x != null) {
            st.nextToken();
            return x;
        } else
            return null;
    }

    private /*@NonNull*/ String expectIdentifier() throws IOException,
            ParseException {
        return expect(matchIdentifier(), "Expected an identifier");
    }

    private int checkNNInteger() {
        if (st.ttype == TT_NUMBER) {
            int x = (int) st.nval;
            if (x == st.nval && x >= 0) // shouldn't give us a huge number
                return x;
        }
        return -1;
    }

    private int matchNNInteger() throws IOException {
        int x = checkNNInteger();
        if (x >= 0) {
            st.nextToken();
            return x;
        } else
            return -1;
    }

    private static final /*@NonNull*/ /*@ReadOnly*/ Set</*@NonNull*/ String>
        primitiveTypeNames;
    static {
        /*@NonNull*/ /*@ReadOnly*/ String[/*@NonNull*/] primitiveTypeNames1 =
                { "byte", "short", "int", "long", "float", "double", "char",
                        "boolean" };
        /*@NonNull*/ Set</*@NonNull*/ String> primitiveTypeNames2 =
            new LinkedHashSet</*@NonNull*/ String>();
        Collections.addAll(primitiveTypeNames2, primitiveTypeNames1);
        primitiveTypeNames = primitiveTypeNames2;
    }

    // HMMM can a (readonly) Integer be casted to a writable Object?
    private <AA extends Annotation> /*@NonNull*/ Object parseScalarAFV(
    /*@NonNull*/ ScalarAFT aft,
    /*@NonNull*/ AnnotationFactory<AA> af) throws IOException, ParseException {
        if (aft instanceof BasicAFT) {
            /*@NonNull*/ Object val;
            /*@NonNull*/ BasicAFT baft = (/*@NonNull*/ BasicAFT) aft;
            /*@NonNull*/ Class<?> type = baft.type;
            if (type == boolean.class) {
                if (matchKeyword("true"))
                    val = true;
                else if (matchKeyword("false"))
                    val = false;
                else
                    throw new ParseException(st.lineno(),
                            "Expected `true' or `false'");
            } else if (type == char.class) {
                if (st.ttype == '\'' && st.sval.length() == 1)
                    val = st.sval.charAt(0);
                else
                    throw new ParseException(st.lineno(),
                            "Expected a character literal");
                st.nextToken();
            } else if (type == String.class) {
                if (st.ttype == '"')
                    val = st.sval;
                else
                    throw new ParseException(st.lineno(),
                            "Expected a string literal");
                st.nextToken();
            } else {
                if (st.ttype == TT_NUMBER) {
                    double n = st.nval;
                    // TODO validate the literal better
                    // HMMM StreamTokenizer can't handle all floating point
                    // numbers; in particular, scientific notation is a problem
                    if (type == byte.class)
                        val = (byte) n;
                    else if (type == short.class)
                        val = (short) n;
                    else if (type == int.class)
                        val = (int) n;
                    else if (type == long.class)
                        val = (long) n;
                    else if (type == float.class)
                        val = (float) n;
                    else if (type == double.class)
                        val = n;
                    else
                        throw new AssertionError();
                    st.nextToken();
                } else
                    throw new ParseException(st.lineno(),
                            "Expected a number literal");
            }
            return val;
        } else if (aft instanceof ClassTokenAFT) {
            expect(st.ttype == TT_WORD,
                    "Expected an identifier or a primitive type");
            /*@NonNull*/ String tokenType;
            // it's a bit hard to tell .class from a class name segment, so
            // if we accidentally read it as part of a fully qualified name,
            // we set this flag
            boolean alreadyAteDotClass = false;
            if (matchKeyword("void"))
                tokenType = "void";
            else {
                if (primitiveTypeNames.contains(st.sval)) {
                    tokenType = st.sval;
                    st.nextToken();
                } else {
                    tokenType = expectIdentifier();
                    while (matchChar('.')) {
                        if (matchKeyword("class")) {
                            alreadyAteDotClass = true;
                            break;
                        } else
                            tokenType += "." + expectIdentifier();
                    }
                }
                if (!alreadyAteDotClass) {
                    // handle array layers
                    while (matchChar('[')) {
                        expectChar(']');
                        tokenType += "[]";
                    }
                }
            }
            if (!alreadyAteDotClass) {
                expectChar('.');
                expectKeyword("class");
            }
            return tokenType;
        } else if (aft instanceof EnumAFT) {
            /*@NonNull*/ String name = expectIdentifier();
            return name;
        } else if (aft instanceof AnnotationAFT) {
            /*@NonNull*/ AnnotationAFT aaft = (/*@NonNull*/ AnnotationAFT) aft;
            /*@NonNull*/ AnnotationDef d = parseAnnotationHead();
            expect(d.name.equals(aaft.annotationDef.name), "Got a(n) " + d.name
                    + " subannotation where a(n) " + aaft.annotationDef.name
                    + " was expected");
            AnnotationBuilder<AA> ab = af.beginAnnotation(d.name);
            // interested in this annotation,
            // so should be interested in subannotations
            assert ab != null;
            AnnotationBuilder<AA> ab2 = (/*@NonNull*/ AnnotationBuilder<AA>) ab;
            AA suba = parseAnnotationBody(d, ab2, af);
            return suba;
        } else
            throw new AssertionError();
    }

    private <AA extends Annotation> void parseAndAddArrayAFV(
    /*@NonNull*/ ArrayAFT aaft,
    /*@NonNull*/ ArrayBuilder arrb, /*@NonNull*/
    AnnotationFactory<AA> af) throws IOException, ParseException {
        /*@NonNull*/ ScalarAFT comp = aaft.elementType;
        expectChar('{');
        while (!matchChar('}')) {
            arrb.appendElement(parseScalarAFV(comp, af));
            if (!checkChar('}'))
                expectChar(',');
        }
        arrb.finish();
    }

    private <AA extends Annotation> void parseAnnotationField(
    /*@NonNull*/ AnnotationDef d,
    /*@NonNull*/ AnnotationBuilder<AA> ab, /*@NonNull*/
    AnnotationFactory<AA> af) throws IOException, ParseException {
        /*@NonNull*/ String fieldName = expectIdentifier();
        // HMMM let's hope the builder checks for duplicate fields
        // because we can't do it any more
        AnnotationFieldType aft1 = d.fieldTypes.get(fieldName);
        expect(aft1 != null, "The annotation type " + d.name
                + " has no field called " + fieldName);
        AnnotationFieldType aft = (/*@NonNull*/ AnnotationFieldType) aft1;
        expectChar('=');
        if (aft instanceof ArrayAFT) {
            /*@NonNull*/ ArrayAFT aaft = (/*@NonNull*/ ArrayAFT) aft;
            if (aaft.elementType == null) {
                // Array of unknown element type--must be zero-length
                expectChar('{');
                expectChar('}');
                ab.addEmptyArrayField(fieldName);
            } else
                parseAndAddArrayAFV(aaft, ab.beginArrayField(fieldName, aaft), af);
        } else if (aft instanceof ScalarAFT) {
            /*@NonNull*/ ScalarAFT saft = (/*@NonNull*/ ScalarAFT) aft;
            /*@NonNull*/ Object value = parseScalarAFV(saft, af);
            ab.addScalarField(fieldName, saft, value);
        } else
            throw new AssertionError();
    }

    private /*@NonNull*/ AnnotationDef parseAnnotationHead() throws IOException,
            ParseException {
        expectChar('@');
        /*@NonNull*/ String name = expectIdentifier();
        while (matchChar('.'))
            name += '.' + expectIdentifier();
        AnnotationDef d = defs.lookup(name);
        expect(d != null, "No definition for annotation type " + name);
        return (/*@NonNull*/ AnnotationDef) d;
    }

    private <AA extends Annotation> /*@NonNull*/ AA parseAnnotationBody(
    /*@NonNull*/ AnnotationDef d,
    /*@NonNull*/ AnnotationBuilder<AA> ab, /*@NonNull*/
    AnnotationFactory<AA> af) throws IOException, ParseException {
        if (matchChar('(')) {
            parseAnnotationField(d, ab, af);
            while (matchChar(','))
                parseAnnotationField(d, ab, af);
            expectChar(')');
        }
        /*@NonNull*/ AA ann = ab.finish();
        expect(ann.def().fieldTypes.size() == d.fieldTypes.size(),
                "At least one annotation field is missing");
        return ann;
    }

    private void parseAnnotations(/*@NonNull*/ AElement<A> e)
            throws IOException, ParseException {
        while (checkChar('@')) {
            /*@NonNull*/ AnnotationDef d = parseAnnotationHead();
            TLAnnotationDef tld = tldefs.lookup(d.name);
            expect(tld != null, "No top-level definition for annotation type "
                    + d.name + " (did you forget the retention policy?)");
            AnnotationBuilder<A> ab = scene.af.beginAnnotation(d.name);
            if (ab == null) {
                // don't care about the result
                // but need to skip over it anyway
                Object trash = parseAnnotationBody(d, SimpleAnnotationFactory.saf
                        .beginAnnotation(d.name),
                        SimpleAnnotationFactory.saf);
            } else {
                A a =
                        parseAnnotationBody(d,
                                (/*@NonNull*/ AnnotationBuilder<A>) ab,
                                scene.af);
                expect(e.tlAnnotationsHere.lookup(a.def().name) == null,
                        "Duplicate annotation of type " + a.def().name);
                TLAnnotation<A> tla = new TLAnnotation<A>(
                        (/*@NonNull*/ TLAnnotationDef) tld, a);
                e.tlAnnotationsHere.add(tla);
            }
        }
    }

    private /*@NonNull*/ AnnotationFieldType parseAFT() throws IOException,
            ParseException {
        ScalarAFT t1 = null;
        /*@NonNull*/ ScalarAFT t2;
        for (/*@NonNull*/ BasicAFT baft : BasicAFT.bafts) {
            if (matchKeyword(baft.toString())) {
                t1 = baft;
                break;
            }
        }
        if (t1 == null) {
            // wasn't a BasicAFT
            if (matchKeyword("unknown")) {
                // Handle unknown[]; see AnnotationBuilder#addEmptyArrayField
                expectChar('[');
                expectChar(']');
                return new ArrayAFT(null);
            } else if (matchKeyword("Class"))
                t2 = ClassTokenAFT.ctaft/* dumpParameterization() */;
            else if (matchKeyword("enum")) {
                /*@NonNull*/ String name = expectIdentifier();
                while (matchChar('.'))
                    name += '.' + expectIdentifier();
                t2 = new EnumAFT(name);
            } else if (matchChar('@')) {
                /*@NonNull*/ String name = expectIdentifier();
                while (matchChar('.'))
                    name += '.' + expectIdentifier();
                AnnotationDef ad = defs.lookup(name);
                expect(ad != null, "Annotation type " + name + " used as a field before it is defined");
                t2 = new AnnotationAFT((/*@NonNull*/ AnnotationDef) ad);
            } else
                throw new ParseException(st.lineno(),
                        "Expected the beginning of an annotation field type: "
                        + "a primitive type, `String', `Class', `enum', or `@'");
        } else
            t2 = (/*@NonNull*/ ScalarAFT) t1;
        if (matchChar('[')) {
            expectChar(']');
            return new ArrayAFT(t2);
        } else
            return t2;
    }

    private void parseAnnotationDef() throws IOException, ParseException {
        expectKeyword("annotation");
        
        RetentionPolicy retention = null;
        for (/*@NonNull*/ RetentionPolicy r : RetentionPolicy.values())
            if (matchKeyword(r.ifname)) {
                retention = r;
                break;
            }
        
        expectChar('@');
        /*@NonNull*/ String basename = expectIdentifier();
        /*@NonNull*/ String fullName = curPkgPrefix + basename;
        
        expect(defs.lookup(fullName) == null, "Duplicate definition of annotation type " + fullName);

        /*@NonNull*/ Map</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType> fields =
                new LinkedHashMap</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType>();

        if (matchChar(':')) {
            // yuck; it would be nicer to do a positive match
            while (st.ttype != TT_EOF && !checkKeyword("annotation")
                    && !checkKeyword("class") && !checkKeyword("package")) {
                /*@NonNull*/ AnnotationFieldType type = parseAFT();
                /*@NonNull*/ String name = expectIdentifier();
                expect(!fields.containsKey(name), "Duplicate definition of field "
                        + name);
                fields.put(name, type);
            }
        }

        /*@NonNull*/ AnnotationDef ad = new AnnotationDef(fullName, fields);
        defs.add(ad);
        
        if (retention != null)
            tldefs.add(new TLAnnotationDef(ad, (/*@NonNull*/ RetentionPolicy) retention));
    }

    private void parseInnerTypes(/*@NonNull*/ ATypeElement<A> e)
            throws IOException, ParseException {
        while (matchKeyword("inner-type")) {
            /*@NonNull*/ ArrayList</*@NonNull*/ Integer> locNumbers =
                    new ArrayList</*@NonNull*/ Integer>();
            locNumbers.add(expectNN(matchNNInteger()));
            while (matchChar(','))
                locNumbers.add(expectNN(matchNNInteger()));
            /*@NonNull*/ InnerTypeLocation loc =
                    new InnerTypeLocation(locNumbers);
            /*@NonNull*/ AElement<A> it = e.innerTypes.vivify(loc);
            expectChar(':');
            parseAnnotations(it);
        }
    }

    private void parseBounds(/*@NonNull*/ VivifyingMap</*@NonNull*/
            BoundLocation, /*@NonNull*/ ATypeElement<A>> bounds)
        throws IOException, ParseException {
        while (matchKeyword("bound")) {
            // expectChar(',');
            int paramIndex = expectNN(matchNNInteger());
            expectChar('&');
            int boundIndex = expectNN(matchNNInteger());
            /*@NonNull*/ BoundLocation bl = new BoundLocation(paramIndex, boundIndex);
            /*@NonNull*/ ATypeElement<A> b = bounds.vivify(bl);
            
            expectChar(':');
            parseAnnotations(b);
            parseInnerTypes(b);
        }
    }

    private void parseField(/*@NonNull*/ AClass<A> c) throws IOException,
            ParseException {
        expectKeyword("field");
        /*@NonNull*/ String name = expectIdentifier();
        /*@NonNull*/ ATypeElement<A> f = c.fields.vivify(name);

        expectChar(':');
        parseAnnotations(f);
        parseInnerTypes(f);
    }

    private void parseMethod(/*@NonNull*/ AClass<A> c) throws IOException,
            ParseException {
        expectKeyword("method");
        // special case: method could be <init> or <clinit>
        /*@NonNull*/ String key;
        if (matchChar('<')) {
            /*@NonNull*/ String basename = expectIdentifier();
            expect(basename.equals("init") || basename.equals("clinit"),
                    "The only special methods allowed are <init> and <clinit>");
            expectChar('>');
            key = "<" + basename + ">";
        } else
            key = expectIdentifier();
        
        expectChar('(');
        key += '(';
        while (!matchChar(':')) {
            if (st.ttype >= 0)
                key += (char) st.ttype;
            else if (st.ttype == TT_WORD)
                key += st.sval;
            else
                throw new ParseException(st.lineno(), "Found something that doesn't belong in a signature");
            st.nextToken();
        }
        
        /*@NonNull*/ AMethod<A> m = c.methods.vivify(key);

        parseAnnotations(m);
        parseInnerTypes(m);
        parseBounds(m.bounds);

        while (matchKeyword("parameter")) {
            expectChar('#');
            int idx = expectNN(matchNNInteger());
            /*@NonNull*/ ATypeElement<A> p = m.parameters.vivify(idx);
            expectChar(':');
            parseAnnotations(p);
            parseInnerTypes(p);
        }
        if (matchKeyword("receiver")) {
            expectChar(':');
            parseAnnotations(m.receiver);
        }
        while (matchKeyword("local")) {
            int index = expectNN(matchNNInteger());
            expectChar('#');
            int scopeStart = expectNN(matchNNInteger());
            expectChar('+');
            int scopeLength = expectNN(matchNNInteger());
            /*@NonNull*/ LocalLocation loc =
                    new LocalLocation(index, scopeStart, scopeLength);
            /*@NonNull*/ ATypeElement<A> l = m.locals.vivify(loc);
            expectChar(':');
            parseAnnotations(l);
            parseInnerTypes(l);
        }
        while (matchKeyword("typecast")) {
            expectChar('#');
            int offset = expectNN(matchNNInteger());
            /*@NonNull*/ ATypeElement<A> t = m.typecasts.vivify(offset);
            expectChar(':');
            parseAnnotations(t);
            parseInnerTypes(t);
        }
        while (matchKeyword("instanceof")) {
            expectChar('#');
            int offset = expectNN(matchNNInteger());
            /*@NonNull*/ ATypeElement<A> i = m.instanceofs.vivify(offset);
            expectChar(':');
            parseAnnotations(i);
            parseInnerTypes(i);
        }
        while (matchKeyword("new")) {
            expectChar('#');
            int offset = expectNN(matchNNInteger());
            /*@NonNull*/ ATypeElement<A> n = m.news.vivify(offset);
            expectChar(':');
            parseAnnotations(n);
            parseInnerTypes(n);
        }
    }

    private void parseClass() throws IOException, ParseException {
        expectKeyword("class");
        /*@NonNull*/ String basename = expectIdentifier();
        /*@NonNull*/ String fullName = curPkgPrefix + basename;

        /*@NonNull*/ AClass<A> c = scene.classes.vivify(fullName);

        expectChar(':');
        parseAnnotations(c);
        parseBounds(c.bounds);

        while (checkKeyword("field"))
            parseField(c);
        while (checkKeyword("method"))
            parseMethod(c);
    }

    private void parse() throws IOException, ParseException {
        st.nextToken();

        while (st.ttype != TT_EOF) {
            expectKeyword("package");

            String pkg = matchIdentifier();
            if (pkg == null) {
                // the default package cannot be annotated
                matchChar(':');
            } else {
                while (matchChar('.'))
                    pkg += '.' + expectIdentifier();
                /*@NonNull*/ AElement<A> p =
                        scene.packages.vivify((/*@NonNull*/ String) pkg);
                expectChar(':');
                parseAnnotations(p);
            }

            if (pkg != null)
                curPkgPrefix = pkg + ".";
            else
                curPkgPrefix = "";

            for (;;) {
                if (checkKeyword("annotation"))
                    parseAnnotationDef();
                else if (checkKeyword("class"))
                    parseClass();
                else if (checkKeyword("package") || st.ttype == TT_EOF)
                    break;
                else
                    throw new ParseException(st.lineno(),
                            "Expected `annotation', `class', or `package'");
            }
        }
    }

    private IndexFileParser(/*@NonNull*/ Reader in, /*@NonNull*/ AScene<A> scene) {
        LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ AnnotationDef> defs1
            = new LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ AnnotationDef>(AnnotationDef.nameKeyer);
        defs = defs1;
        LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef> tldefs1
            = new LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef>(TLAnnotationDef.nameKeyer);
        tldefs = tldefs1;
        
        st = new StreamTokenizer(in);
        st.slashSlashComments(true);

        // restrict numbers -- don't really need, could interfere with
        // annotation values
        // st.ordinaryChar('-');
        // HMMM this fixes fully-qualified-name strangeness but breaks
        // floating-point numbers
        st.ordinaryChar('.');
        
        // argggh!!! stupid default needs to be overridden! see java bug 4217680
        st.ordinaryChar('/');

        // for "type-argument"
        st.wordChars('-', '-');

        // java identifiers can contain numbers, _, and $
        st.wordChars('0', '9');
        st.wordChars('_', '_');
        st.wordChars('$', '$');

        this.scene = scene;
        
        // See if the nonnull analysis picks up on this:
        // curPkgPrefix == ""; // will get changed later anyway
    }

    /**
     * Reads annotations from <code>in</code> in index file format and merges
     * them into <code>scene</code>, using
     * <code>scene.</code>{@link AScene#af af} to build individual annotations.
     * If <code>scene.</code>{@link AScene#af af} is uninterested in an
     * annotation found in the input, that annotation is skipped. Annotations
     * from the input are merged into the scene; it is an error if both the
     * scene and the input contain annotations of the same type on the same
     * element.
     * 
     * <p>
     * Since each annotation in a scene carries its own definition and the
     * scene as a whole no longer has a set of definitions, annotation
     * definitions that are given in the input but never used are not saved
     * anywhere and will not be included if the scene is written back to an
     * index file.  Similarly, retention policies on definitions of annotations
     * that are never used at the top level are dropped.
     * 
     * <p>
     * Caveat: Parsing of floating point numbers currently does not work.
     */
    public static <A extends Annotation> void parse(/*@NonNull*/ Reader in, /*@NonNull*/
    AScene<A> scene) throws IOException, ParseException {
        IndexFileParser<A> parser = new IndexFileParser<A>(in, scene);
        parser.parse();
    }

    /**
     * Reads annotations from the index file <code>filename</code> and merges
     * them into <code>scene</code>; see {@link #parse(Reader, AScene)}.
     */
    public static <A extends Annotation> void parse(
    /*@NonNull*/ String filename,
    /*@NonNull*/ AScene<A> scene) throws IOException, FileParseException {
        /*@NonNull*/ Reader in = new FileReader(filename);
        try {
            parse(in, scene);
        } catch (ParseException e) {
            throw new FileParseException(e, filename);
        }
    }
}
