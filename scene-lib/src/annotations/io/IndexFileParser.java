package annotations.io;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_NUMBER;
import static java.io.StreamTokenizer.TT_WORD;

import java.io.*;
import java.util.*;

import plume.FileIOException;

import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.util.coll.*;

/**
 * IndexFileParser provides static methods
 * {@link #parse(LineNumberReader, AScene)},
 * {@link #parseFile(String, AScene)}, and
 * {@link #parseString(String, AScene)}.
 * Each of these parses an index file into a {@link AScene}.
 * <p>
 *
 * If there are any problems, it throws a ParseException internally, or a
 * FileIOException externally.
 */
public final class IndexFileParser {

    // The input
    private final StreamTokenizer st;

    // The output
    private final AScene scene;

    private String curPkgPrefix;

    /**
     * Holds definitions we've seen so far.  Maps from annotation name to
     * the definition itself.  Maps from both the qualified name and the
     * unqualified name.  If the unqualified name is not unique, it maps
     * to null and the qualified name should be used instead. */
    private final HashMap<String, AnnotationDef> defs;

    private int expectNonNegative(int i) throws ParseException {
        if (i >= 0)
            return i;
        else
            throw new ParseException("Expected a nonnegative integer, got " + st);
    }

    /** True if the next thing from st is the given character. */
    private boolean checkChar(char c) /*@ReadOnly*/ {
        return st.ttype == c;
    }

    /** True if the next thing from st is the given string token. */
    private boolean checkKeyword(String s) /*@ReadOnly*/ {
        return st.ttype == TT_WORD && st.sval.equals(s);
    }

    /**
     * Return true if the next thing to be read from st is the given string.
     * In that case, also read past the given string.
     * If the result is false, reads nothing from st.
     */
    private boolean matchChar(char c) throws IOException {
        if (checkChar(c)) {
            st.nextToken();
            return true;
        } else
            return false;
    }

    /**
     * Return true if the next thing to be read from st is the given string.
     * In that case, also read past the given string.
     * If the result is false, reads nothing from st.
     */
    private boolean matchKeyword(String s) throws IOException {
        if (checkKeyword(s)) {
            st.nextToken();
            return true;
        } else
            return false;
    }

    /** Reads from st.  If the result is not c, throws an exception. */
    private void expectChar(char c) throws IOException, ParseException {
        if (! matchChar(c)) {
            // Alternately, could use st.toString().
            String found;
            switch (st.ttype) {
            case StreamTokenizer.TT_WORD: found = st.sval; break;
            case StreamTokenizer.TT_NUMBER: found = "" + st.nval; break;
            case StreamTokenizer.TT_EOL: found = "end of line"; break;
            case StreamTokenizer.TT_EOF: found = "end of file"; break;
            default: found = "'" + ((char) st.ttype) + "'"; break;
            }
            throw new ParseException("Expected '" + c + "', found " + found);
        }
    }

    /** Reads from st.  If the result is not s, throws an exception. */
    private void expectKeyword(String s) throws IOException,
            ParseException {
        if (! matchKeyword(s)) {
            throw new ParseException("Expected `" + s + "'");
        }
    }

    private static final /*@ReadOnly*/ Set<String> knownKeywords;
    static {
        /*@ReadOnly*/ String[] knownKeywords_array =
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
        knownKeywords = new LinkedHashSet<String>();
        Collections.addAll(knownKeywords, knownKeywords_array);
    }

    private boolean isValidIdentifier(String x) /*@ReadOnly*/ {
        if (x.length() == 0 || !Character.isJavaIdentifierStart(x.charAt(0))
                || knownKeywords.contains(x))
            return false;
        for (int i = 1; i < x.length(); i++)
            if (!Character.isJavaIdentifierPart(x.charAt(i)))
                return false;
        return true;
    }

    private String checkIdentifier() /*@ReadOnly*/ {
        if (st.sval == null)
            return null;
        else {
            String val = st.sval;
            if (st.ttype == TT_WORD && isValidIdentifier(val))
                return st.sval;
            else
                return null;
        }
    }

    private String matchIdentifier() throws IOException {
        String x = checkIdentifier();
        if (x != null) {
            st.nextToken();
            return x;
        } else
            return null;
    }

    private String expectIdentifier() throws IOException, ParseException {
        String id = matchIdentifier();
        if (id == null) { throw new ParseException("Expected an identifier"); }
        return id;
    }

    // an identifier, or a sequence of dot-separated identifiers
    private String expectQualifiedName() throws IOException, ParseException {
        String name = expectIdentifier();
        while (matchChar('.'))
            name += '.' + expectIdentifier();
        return name;
    }

    private int checkNNInteger() /*@ReadOnly*/ {
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

    // Mapping from primitive types and void to their corresponding
    // class objects. Class.forName doesn't directly support these.
    // Using this map we can go from "void.class" to the correct
    // Class object.
    private static final /*@ReadOnly*/ Map<String, Class<?>> primitiveTypes;
    static {
        Map<String, Class<?>> pt = new LinkedHashMap<String, Class<?>>();
        pt.put("byte", byte.class);
        pt.put("short", short.class);
        pt.put("int", int.class);
        pt.put("long", long.class);
        pt.put("float", float.class);
        pt.put("double", double.class);
        pt.put("char", char.class);
        pt.put("boolean", boolean.class);
        pt.put("void", void.class);
        primitiveTypes = pt;
    }

    /** Parse scalar annotation value. */
    // HMMM can a (readonly) Integer be casted to a writable Object?
    private /*@ReadOnly*/ Object parseScalarAFV(ScalarAFT aft) throws IOException, ParseException {
        if (aft instanceof BasicAFT) {
            /*@ReadOnly*/ Object val;
            BasicAFT baft = (BasicAFT) aft;
            Class<?> type = baft.type;
            if (type == boolean.class) {
                if (matchKeyword("true"))
                    val = true;
                else if (matchKeyword("false"))
                    val = false;
                else
                    throw new ParseException("Expected `true' or `false'");
            } else if (type == char.class) {
                if (st.ttype == '\'' && st.sval.length() == 1)
                    val = st.sval.charAt(0);
                else
                    throw new ParseException("Expected a character literal");
                st.nextToken();
            } else if (type == String.class) {
                if (st.ttype == '"')
                    val = st.sval;
                else
                    throw new ParseException("Expected a string literal");
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
                    throw new ParseException(
                            "Expected a number literal");
            }
            assert aft.isValidValue(val);
            return val;
        } else if (aft instanceof ClassTokenAFT) {
            // Expect the class name in the format that Class.forName accepts,
            // which is some very strange format.
            // Example inputs followed by their Java source ".class" equivalent:
            //   [[I.class      for int[][].class
            //   [java.util.Map for Map[].class
            //   java.util.Map  for Map.class
            // Have to use fully-qualified names, i.e. "Object" alone won't work.
            // Also note use of primitiveTypes map for primitives and void.
            int arrays = 0;
            StringBuilder type = new StringBuilder();
            while (matchChar('[')) {
                // Array dimensions as prefix
                ++arrays;
            }
            while (!matchKeyword("class")) {
                if (st.ttype >= 0)
                    type.append((char) st.ttype);
                else if (st.ttype == TT_WORD)
                    type.append(st.sval);
                else
                    throw new ParseException("Found something that doesn't belong in a signature");
                st.nextToken();
            }

            // Drop the '.' before the "class"
            type.deleteCharAt(type.length()-1);
            // expectKeyword("class");

            // Add arrays as prefix in the type.
            while (arrays-->0) {
                type.insert(0, '[');
            }

            try {
                String sttype = type.toString();
                Class<?> tktype;
                if (primitiveTypes.containsKey(sttype)) {
                    tktype = primitiveTypes.get(sttype);
                } else {
                    tktype = Class.forName(sttype);
                }
                assert aft.isValidValue(tktype);
                return tktype;
            } catch (ClassNotFoundException e) {
                throw new ParseException("Could not load class: " + type, e);
            }
        } else if (aft instanceof EnumAFT) {
            String name = expectQualifiedName();
            assert aft.isValidValue(name);
            return name;
        } else if (aft instanceof AnnotationAFT) {
            AnnotationAFT aaft = (AnnotationAFT) aft;
            AnnotationDef d = parseAnnotationHead();
            if (! d.name.equals(aaft.annotationDef.name)) {
                throw new ParseException("Got an " + d.name
                    + " subannotation where an " + aaft.annotationDef.name
                    + " was expected");
            }
            AnnotationBuilder ab = AnnotationFactory.saf.beginAnnotation(d);
            // interested in this annotation,
            // so should be interested in subannotations
            assert ab != null;
            AnnotationBuilder ab2 = (AnnotationBuilder) ab;
            Annotation suba = parseAnnotationBody(d, ab2);
            assert aft.isValidValue(suba);
            return suba;
        } else {
            throw new AssertionError("IndexFileParser.parseScalarAFV: unreachable code.");
        }
    }

    private void parseAndAddArrayAFV(ArrayAFT aaft, ArrayBuilder arrb) throws IOException, ParseException {
        ScalarAFT comp;
        if (aaft.elementType != null)
            comp = aaft.elementType;
        else
            throw new IllegalArgumentException("array AFT has null elementType");
        if (matchChar('{')) {
            // read an array
            while (!matchChar('}')) {
                arrb.appendElement(parseScalarAFV(comp));
                if (!checkChar('}'))
                    expectChar(',');
            }
        } else {
            // not an array, so try reading just one value as an array
            arrb.appendElement(parseScalarAFV(comp));
        }
        arrb.finish();
    }

    // parses a field such as "f1=5" in "@A(f1=5, f2=10)".
    private void parseAnnotationField(AnnotationDef d, AnnotationBuilder ab) throws IOException, ParseException {
        String fieldName;
        if (d.fieldTypes.size() == 1
            && d.fieldTypes.containsKey("value")) {
            fieldName = "value";
            if (matchKeyword("value")) {
                expectChar('=');
            }
        } else {
            fieldName = expectIdentifier();
            expectChar('=');
        }
        // HMMM let's hope the builder checks for duplicate fields
        // because we can't do it any more
        AnnotationFieldType aft1 = d.fieldTypes.get(fieldName);
        if (aft1 == null) {
            throw new ParseException("The annotation type " + d.name
                + " has no field called " + fieldName);
        }
        AnnotationFieldType aft = (AnnotationFieldType) aft1;
        if (aft instanceof ArrayAFT) {
            ArrayAFT aaft = (ArrayAFT) aft;
            if (aaft.elementType == null) {
                // Array of unknown element type--must be zero-length
                expectChar('{');
                expectChar('}');
                ab.addEmptyArrayField(fieldName);
            } else
                parseAndAddArrayAFV(aaft, ab.beginArrayField(fieldName, aaft));
        } else if (aft instanceof ScalarAFT) {
            ScalarAFT saft = (ScalarAFT) aft;
            /*@ReadOnly*/ Object value = parseScalarAFV(saft);
            ab.addScalarField(fieldName, saft, value);
        } else
            throw new AssertionError();
    }

    // reads the "@A" part of an annotation such as "@A(f1=5, f2=10)".
    private AnnotationDef parseAnnotationHead() throws IOException,
            ParseException {
        expectChar('@');
        String name = expectQualifiedName();
        AnnotationDef d = defs.get(name);
        if (d == null) {
            if (false) {
                System.err.println("No definition for annotation type " + name);
                System.err.printf("  defs contains %d entries%n", defs.size());
                for (Map.Entry<String,AnnotationDef> entry : defs.entrySet()) {
                    System.err.printf("    defs entry: %s => %s%n", entry.getKey(), entry.getValue());
                }
            }
            throw new ParseException("No definition for annotation type " + name);
        }
        return d;
    }

    private Annotation parseAnnotationBody(AnnotationDef d, AnnotationBuilder ab) throws IOException, ParseException {
        if (matchChar('(')) {
            parseAnnotationField(d, ab);
            while (matchChar(','))
                parseAnnotationField(d, ab);
            expectChar(')');
        }
        Annotation ann = ab.finish();
        if (! ann.def.equals(d)) {
            throw new ParseException(
                "parseAnnotationBody: Annotation def isn't as it should be.\n" + d + "\n" + ann.def);
        }
        if (ann.def().fieldTypes.size() != d.fieldTypes.size()) {
            throw new ParseException(
                "At least one annotation field is missing");
        }
        return ann;
    }

    private void parseAnnotations(AElement e)
            throws IOException, ParseException {
        while (checkChar('@')) {
            AnnotationDef d = parseAnnotationHead();
            AnnotationBuilder ab = AnnotationFactory.saf.beginAnnotation(d);
            if (ab == null) {
                // don't care about the result
                // but need to skip over it anyway
                /*@ReadOnly*/ Object trash = parseAnnotationBody(d, AnnotationFactory.saf
                        .beginAnnotation(d));
            } else {
                Annotation a = parseAnnotationBody(d, ab);
                for (Annotation other : e.tlAnnotationsHere) {
                    if (a.def.name.equals(other.def.name)) {
                        throw new ParseException(
                          "Duplicate annotation of type " + a.def().name);
                    }
                }
                Annotation tla = a;
                if (! tla.def.equals(d)) {
                    throw new ParseException("Bad def");
                }
                e.tlAnnotationsHere.add(tla);
            }
        }
    }

    private ScalarAFT parseScalarAFT() throws IOException, ParseException {
        for (BasicAFT baft : BasicAFT.bafts.values()) {
            if (matchKeyword(baft.toString())) {
                return baft;
            }
        }
        // wasn't a BasicAFT
        if (matchKeyword("Class")) {
            return ClassTokenAFT.ctaft/* dumpParameterization() */;
        } else if (matchKeyword("enum")) {
            String name = expectQualifiedName();
            return new EnumAFT(name);
        } else if (matchKeyword("annotation-field")) {
            String name = expectQualifiedName();
            AnnotationDef ad = defs.get(name);
            if (ad == null) {
                throw new ParseException("Annotation type " + name + " used as a field before it is defined");
            }
            return new AnnotationAFT((AnnotationDef) ad);
        } else {
            throw new ParseException(
                    "Expected the beginning of an annotation field type: "
                    + "a primitive type, `String', `Class', `enum', or `annotation-field'");
        }
    }

    private AnnotationFieldType parseAFT() throws IOException,
            ParseException {
        if (matchKeyword("unknown")) {
            // Handle unknown[]; see AnnotationBuilder#addEmptyArrayField
            expectChar('[');
            expectChar(']');
            return new ArrayAFT(null);
        }
        ScalarAFT baseAFT = parseScalarAFT();
        // only one level of array is permitted
        if (matchChar('[')) {
            expectChar(']');
            return new ArrayAFT(baseAFT);
        } else
            return baseAFT;
    }

    private void parseAnnotationDef() throws IOException, ParseException {
        expectKeyword("annotation");

        expectChar('@');
        String basename = expectIdentifier();
        String fullName = curPkgPrefix + basename;

        AnnotationDef ad = new AnnotationDef(fullName);
        expectChar(':');
        parseAnnotations(ad);

        Map<String, AnnotationFieldType> fields =
                new LinkedHashMap<String, AnnotationFieldType>();

        // yuck; it would be nicer to do a positive match
        while (st.ttype != TT_EOF && !checkKeyword("annotation")
               && !checkKeyword("class") && !checkKeyword("package")) {
            AnnotationFieldType type = parseAFT();
            String name = expectIdentifier();
            if (fields.containsKey(name)) {
                throw new ParseException("Duplicate definition of field "
                                         + name);
            }
            fields.put(name, type);
        }

        ad.setFieldTypes(fields);

        // Now add the definition to the map of all definitions.
        addDef(ad, basename);

    }

    // Add the definition to the map of all definitions.
    // also see addDef(AnnotationDef, String).
    public void addDef(AnnotationDef ad) throws ParseException {
        String basename = ad.name;
        int dotPos = basename.lastIndexOf('.');
        if (dotPos != -1) {
            basename = basename.substring(dotPos + 1);
        }
        addDef(ad, basename);
    }

    // Add the definition to the map of all definitions.
    public void addDef(AnnotationDef ad, String basename) throws ParseException {
        // System.out.println("addDef:" + ad);

        if (defs.containsKey(ad.name)) {
            // TODO:  permit identical re-definition
            System.err.println("Duplicate definition of annotation type " + ad.name);
        }
        defs.put(ad.name, ad);
        // Add short name; but if it's already there, remove it to avoid ambiguity.
        if (! basename.equals(ad.name)) {
            if (defs.containsKey(basename))
                // not "defs.remove(basename)" because then a subsequent
                // one could get added, which would be wrong.
                defs.put(basename, null);
            else
                defs.put(basename, ad);
        }
    }


    private void parseInnerTypes(ATypeElement e)
            throws IOException, ParseException {
        while (matchKeyword("inner-type")) {
            ArrayList<Integer> locNumbers =
                    new ArrayList<Integer>();
            locNumbers.add(expectNonNegative(matchNNInteger()));
            while (matchChar(','))
                locNumbers.add(expectNonNegative(matchNNInteger()));
            InnerTypeLocation loc =
                    new InnerTypeLocation(locNumbers);
            AElement it = e.innerTypes.vivify(loc);
            expectChar(':');
            parseAnnotations(it);
        }
    }

    private void parseBounds(VivifyingMap<BoundLocation, ATypeElement> bounds)
        throws IOException, ParseException {
        while (checkKeyword("typeparam") || checkKeyword("bound")) {
            if (matchKeyword("typeparam")) {
                int paramIndex = expectNonNegative(matchNNInteger());
                BoundLocation bl = new BoundLocation(paramIndex, -1);
                ATypeElement b = bounds.vivify(bl);
                expectChar(':');
                parseAnnotations(b);
                // does this make sense?
                parseInnerTypes(b);
            } else if (matchKeyword("bound")) {
                // expectChar(',');
                int paramIndex = expectNonNegative(matchNNInteger());
                expectChar('&');
                int boundIndex = expectNonNegative(matchNNInteger());
                BoundLocation bl = new BoundLocation(paramIndex, boundIndex);
                ATypeElement b = bounds.vivify(bl);
                expectChar(':');
                parseAnnotations(b);
                // does this make sense?
                parseInnerTypes(b);
            } else {
                throw new Error("impossible");
            }
        }
    }

    private void parseExtends(AClass cls) throws IOException, ParseException {
        expectKeyword("extends");
        TypeIndexLocation idx = new TypeIndexLocation(-1);
        ATypeElement ext = cls.extendsImplements.vivify(idx);
        expectChar(':');
        parseAnnotations(ext);
        parseInnerTypes(ext);
    }

    private void parseImplements(AClass cls) throws IOException, ParseException {
        expectKeyword("implements");
        int implIndex = expectNonNegative(matchNNInteger());
        TypeIndexLocation idx = new TypeIndexLocation(implIndex);
        ATypeElement impl = cls.extendsImplements.vivify(idx);
        expectChar(':');
        parseAnnotations(impl);
        parseInnerTypes(impl);
    }

    private void parseField(AClass c) throws IOException,
            ParseException {
        expectKeyword("field");
        String name = expectIdentifier();
        AElement f = c.fields.vivify(name);

        expectChar(':');
        parseAnnotations(f);
        if (checkKeyword("type") && matchKeyword("type")) {
            expectChar(':');
            parseAnnotations(f.type);
            parseInnerTypes(f.type);
        }

        AExpression fieldinit = c.fieldInits.vivify(name);
        parseExpression(fieldinit);
    }

    private void parseStaticInit(AClass c) throws IOException,
            ParseException {
        expectKeyword("staticinit");
        expectChar('*');
        int blockIndex = expectNonNegative(matchNNInteger());
        expectChar(':');

        ABlock staticinit = c.staticInits.vivify(blockIndex);
        parseBlock(staticinit);
    }

    private void parseMethod(AClass c) throws IOException,
            ParseException {
        expectKeyword("method");
        // special case: method could be <init> or <clinit>
        String key;
        if (matchChar('<')) {
            String basename = expectIdentifier();
            if (!(basename.equals("init") || basename.equals("clinit"))) {
                throw new ParseException(
                    "The only special methods allowed are <init> and <clinit>");
            }
            expectChar('>');
            key = "<" + basename + ">";
        } else {
            key = expectIdentifier();
        }

        expectChar('(');
        key += '(';
        while (!matchChar(':')) {
            if (st.ttype >= 0)
                key += (char) st.ttype;
            else if (st.ttype == TT_WORD)
                key += st.sval;
            else
                throw new ParseException("Found something that doesn't belong in a signature");
            st.nextToken();
        }

        AMethod m = c.methods.vivify(key);

        parseAnnotations(m);

        parseBounds(m.bounds);

        // Permit return value, receiver, and parameters in any order.
        while (checkKeyword("return") || checkKeyword("receiver") || checkKeyword("parameter")) {
            if (matchKeyword("return")) {
                expectChar(':');
                parseAnnotations(m.returnType);
                parseInnerTypes(m.returnType);
            } else if (matchKeyword("parameter")) {
                // make "#" optional
                if (checkChar('#')) {
                    matchChar('#');
                }
                int idx = expectNonNegative(matchNNInteger());
                AElement p = m.parameters.vivify(idx);
                expectChar(':');
                parseAnnotations(p);
                if (checkKeyword("type") && matchKeyword("type")) {
                    expectChar(':');
                    parseAnnotations(p.type);
                    parseInnerTypes(p.type);
                }
            } else if (matchKeyword("receiver")) {
                expectChar(':');
                parseAnnotations(m.receiver);
                parseInnerTypes(m.receiver);
            } else {
                throw new Error("This can't happen");
            }
        }

        parseBlock(m);
    }

    private void parseBlock(ABlock bl) throws IOException,
            ParseException {
        boolean matched = true;

        while (matched) {
            matched = false;

            while (checkKeyword("local")) {
                matchKeyword("local");
                matched = true;
                LocalLocation loc;
                if (checkNNInteger() != -1) {
                    // the local variable is specified by bytecode index/range
                    int index = expectNonNegative(matchNNInteger());
                    expectChar('#');
                    int scopeStart = expectNonNegative(matchNNInteger());
                    expectChar('+');
                    int scopeLength = expectNonNegative(matchNNInteger());
                    loc = new LocalLocation(index, scopeStart, scopeLength);
                } else {
                    // look for a valid identifier for the local variable
                    String lvar = expectIdentifier();
                    int varIndex;
                    if (checkChar('*')) {
                        expectChar('*');
                        varIndex = expectNonNegative(matchNNInteger());
                    } else {
                        // default the variable index to 0, the most common case
                        varIndex = 0;
                    }
                    loc = new LocalLocation(lvar, varIndex);
                }
                AElement l = bl.locals.vivify(loc);
                expectChar(':');
                parseAnnotations(l);
                if (checkKeyword("type") && matchKeyword("type")) {
                    expectChar(':');
                    parseAnnotations(l.type);
                    parseInnerTypes(l.type);
                }
            }
            matched = parseExpression(bl) || matched;
        }
    }

    private boolean parseExpression(AExpression exp) throws IOException,
            ParseException {
        boolean matched = true;
        boolean evermatched = false;

        while (matched) {
            matched = false;

            while (checkKeyword("typecast")) {
                matchKeyword("typecast");
                matched = true;
                evermatched = true;
                RelativeLocation loc;
                if (checkChar('#')) {
                    expectChar('#');
                    int offset = expectNonNegative(matchNNInteger());
                    loc = RelativeLocation.createOffset(offset);
                } else {
                    expectChar('*');
                    int index = expectNonNegative(matchNNInteger());
                    loc = RelativeLocation.createIndex(index);
                }
                ATypeElement t = exp.typecasts.vivify(loc);
                expectChar(':');
                parseAnnotations(t);
                parseInnerTypes(t);
            }
            while (checkKeyword("instanceof")) {
                matchKeyword("instanceof");
                matched = true;
                evermatched = true;
                RelativeLocation loc;
                if (checkChar('#')) {
                    expectChar('#');
                    int offset = expectNonNegative(matchNNInteger());
                    loc = RelativeLocation.createOffset(offset);
                } else {
                    expectChar('*');
                    int index = expectNonNegative(matchNNInteger());
                    loc = RelativeLocation.createIndex(index);
                }
                ATypeElement i = exp.instanceofs.vivify(loc);
                expectChar(':');
                parseAnnotations(i);
                parseInnerTypes(i);
            }
            while (checkKeyword("new")) {
                matchKeyword("new");
                matched = true;
                evermatched = true;
                RelativeLocation loc;
                if (checkChar('#')) {
                    expectChar('#');
                    int offset = expectNonNegative(matchNNInteger());
                    loc = RelativeLocation.createOffset(offset);
                } else {
                    expectChar('*');
                    int index = expectNonNegative(matchNNInteger());
                    loc = RelativeLocation.createIndex(index);
                }
                ATypeElement n = exp.news.vivify(loc);
                expectChar(':');
                parseAnnotations(n);
                parseInnerTypes(n);
            }
        }
        return evermatched;
    }

    private void parseClass() throws IOException, ParseException {
        expectKeyword("class");
        String basename = expectIdentifier();
        String fullName = curPkgPrefix + basename;

        AClass c = scene.classes.vivify(fullName);
        expectChar(':');

        parseAnnotations(c);
        parseBounds(c.bounds);

        while (checkKeyword("extends"))
            parseExtends(c);
        while (checkKeyword("implements"))
            parseImplements(c);

        while (checkKeyword("field"))
            parseField(c);
        while (checkKeyword("staticinit"))
            parseStaticInit(c);
        while (checkKeyword("method"))
            parseMethod(c);
        c.methods.prune();
    }

    // Reads the index file in this.st and puts the information in this.scene.
    private void parse() throws ParseException, IOException {
        st.nextToken();

        while (st.ttype != TT_EOF) {
            expectKeyword("package");

            String pkg;
            if (checkIdentifier() == null) {
                pkg = null;
                // the default package cannot be annotated
                matchChar(':');
            } else {
                pkg = expectQualifiedName();
                AElement p = scene.packages.vivify(pkg);
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
                    throw new ParseException(
                                             "Expected `annotation', `class', or `package', found `" + st.sval + "', ttype:" + st.ttype);
            }
        }
    }

    private IndexFileParser(Reader in, AScene scene) {
        defs = new LinkedHashMap<String, AnnotationDef>();
        for (AnnotationDef ad : Annotations.standardDefs) {
            try {
                addDef(ad);
            } catch (ParseException e) {
                throw new Error(e);
            }
        }

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
     * them into <code>scene</code>.  Annotations
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
    public static void parse(LineNumberReader in, AScene scene)
        throws IOException, ParseException {
        IndexFileParser parser = new IndexFileParser(in, scene);
        // no filename is available in the exception messages
        try {
            parser.parse();
        } catch (IOException e) {
            throw new FileIOException(in, e);
        } catch (ParseException e) {
            throw new FileIOException(in, e);
        }
    }

    /**
     * Reads annotations from the index file <code>filename</code> and merges
     * them into <code>scene</code>; see {@link #parse(LineNumberReader, AScene)}.
     */
    public static void parseFile(String filename, AScene scene)
        throws IOException {
        LineNumberReader in = new LineNumberReader(new FileReader(filename));
        IndexFileParser parser = new IndexFileParser(in, scene);
        try {
            parser.parse();
        } catch (IOException e) {
            throw new FileIOException(in, filename, e);
        } catch (ParseException e) {
            throw new FileIOException(in, filename, e);
        }
    }

    /**
     * Reads annotations from the string (in index file format) and merges
     * them into <code>scene</code>; see {@link #parse(LineNumberReader, AScene)}.
     * Primarily for testing.
     */
    public static void parseString(String fileContents, AScene scene)
        throws IOException {
        String filename = "While parsing string: \n----------------BEGIN----------------\n" + fileContents + "----------------END----------------\n";
        LineNumberReader in = new LineNumberReader(new StringReader(fileContents));
        try {
            IndexFileParser parser = new IndexFileParser(in, scene);
            parser.parse();
        } catch (IOException e) {
            throw new FileIOException(in, filename, e);
        } catch (ParseException e) {
            throw new FileIOException(in, filename, e);
        }
    }

}
