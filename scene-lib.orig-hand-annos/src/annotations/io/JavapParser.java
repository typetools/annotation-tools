package annotations.io;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import annotations.*;
import annotations.el.*;

import static annotations.io.IOUtils.*;

/**
 * <code>JavapParser</code> provides a static method that parses a class dump
 * in the form produced by <code>xjavap -s -verbose -annotations</code> and adds
 * the annotations to an {@link AScene}, using the scene's
 * {@link AnnotationFactory} to build individual annotations.
 * If the scene's {@link AnnotationFactory} announces that it does not want an
 * annotation found in the javap output, that annotation is skipped. Annotations
 * from the javap output are merged into the scene; it is an error if both the
 * scene and the javap output contain annotations of the same type on the same
 * element.
 * 
 * <p>
 * THIS CLASS IS NOT FINISHED YET!
 * 
 * <p>
 * This class does not yet perform any error checking.  Expect strange
 * behavior and/or exceptions if you give it bad input.
 */
public final class JavapParser<A extends Annotation> {
    private static final /*@NonNull*/ String SECTION_TITLE_PREFIX = "  ";
    private static final /*@NonNull*/ String SECTION_DATA_PREFIX = "   ";
    private static final /*@NonNull*/ String CONST_POOL_DATA_PREFIX = "const #";
    
    private final /*@NonNull*/ AScene<A> scene;
    
    private final /*@NonNull*/ BufferedReader bin;
    private String line; // null means end-of-file
    
    private int lineNo = 0; // TEMP
    
    private void nextLine() throws IOException {
        do {
            line = bin.readLine();
            lineNo++;
        } while (line != null && line.equals(""));
    }
    
    private void trim(String prefix) {
        if (line.startsWith(prefix))
            line = line.substring(prefix.length());
    }
    
    private boolean inMember() {
        return line.startsWith(SECTION_TITLE_PREFIX);
    }
    
    private boolean inData() {
        return line.startsWith(SECTION_DATA_PREFIX) ||
            line.startsWith(CONST_POOL_DATA_PREFIX);
    }
    
    private enum TargetMode {
        ORIGINAL, PARAMETER, EXTENDED
    }
    
    private enum AnnotationSection {
        RVA("RuntimeVisibleAnnotations", RetentionPolicy.RUNTIME, TargetMode.ORIGINAL),
        RIA("RuntimeInvisibleAnnotations", RetentionPolicy.CLASS, TargetMode.ORIGINAL),
        RVPA("RuntimeVisibleParameterAnnotations", RetentionPolicy.RUNTIME, TargetMode.PARAMETER),
        RIPA("RuntimeInvisibleParameterAnnotations", RetentionPolicy.CLASS, TargetMode.PARAMETER),
        RVEA("RuntimeVisibleTypeAnnotations", RetentionPolicy.RUNTIME, TargetMode.EXTENDED),
        RIEA("RuntimeInvisibleTypeAnnotations", RetentionPolicy.CLASS, TargetMode.EXTENDED),
        ;
        
        final /*@NonNull*/ String secTitle;
        final /*@NonNull*/ RetentionPolicy retention;
        final /*@NonNull*/ TargetMode locMode;
        
        AnnotationSection(/*@NonNull*/ String secTitle, /*@NonNull*/ RetentionPolicy retention, /*@NonNull*/ TargetMode locMode) {
            this.secTitle = secTitle;
            this.retention = retention;
            this.locMode = locMode;
        }
    }
    
    private /*@NonNull*/ String parseAnnotationHead() throws IOException, ParseException {
        /*@NonNull*/ String annoTypeName = line.substring(
                line.indexOf(annotationHead) + annotationHead.length(),
                line.length() - 1).replace('/', '.');
        nextLine();
        return annoTypeName;
    }
    
    private static final /*@NonNull*/ String annotationHead = "//Annotation L"; // TEMP
    private static final /*@NonNull*/ String tagHead = "type = "; // TEMP
    
    private <AA extends Annotation> /*@NonNull*/ AA parseAnnotationBody(
            /*@NonNull*/ AnnotationBuilder<AA> ab,
            /*@NonNull*/ AnnotationFactory<AA> af,
            /*@NonNull*/ String indent) throws IOException, ParseException {
        // Grab the fields
        /*@NonNull*/ String fieldIndent = indent + " ";
        while (line.startsWith(fieldIndent)) {
            /*@NonNull*/ String line2 = line.substring(fieldIndent.length());
            // Let the caller deal with location information, if any
            if (line2.startsWith("target") || line2.startsWith("parameter"))
                break;
            /*@NonNull*/ String fieldName =
                line2.substring(line2.indexOf("//") + "//".length());
            nextLine();
            char tag = line.charAt(line.indexOf(tagHead) + tagHead.length());
            switch (tag) {
            case '[':
                break;
            case '@':
                break;
            case 'c':
                break;
            case 'e':
                break;
            }
            // FINISH
        }
        return ab.finish();
    }
    
    private static final /*@NonNull*/ String paramIdxHead = "parameter = ";
    private static final /*@NonNull*/ String offsetHead = "offset = ";
    private static final /*@NonNull*/ Pattern localLocRegex =
        Pattern.compile("^\\s*start_pc = (\\d+), length = (\\d+), index = (\\d+)$");
    private static final /*@NonNull*/ String itlnHead = "location = ";
    
    private int parseOffset() throws IOException, ParseException {
        int offset = Integer.parseInt(
                line.substring(line.indexOf(offsetHead) + offsetHead.length()));
        nextLine();
        return offset;
    }
    
    private /*@NonNull*/ List<Integer> parseInnerTypeLocationNums() throws IOException, ParseException {
        /*@NonNull*/ String numsStr
            = line.substring(line.indexOf(itlnHead) + itlnHead.length());
        /*@NonNull*/ List<Integer> nums = new ArrayList<Integer>();
        for (;;) {
            int comma = numsStr.indexOf(',');
            if (comma == -1) {
                nums.add(Integer.parseInt(numsStr));
                break;
            }
            nums.add(Integer.parseInt(numsStr.substring(0, comma)));
            numsStr = numsStr.substring(comma + 2);
        }
        nextLine();
        return nums;
    }
    
    private /*@NonNull*/ AElement<A> chooseSubElement(/*@NonNull*/ AElement<A> member, /*@NonNull*/ AnnotationSection sec) throws IOException, ParseException {
        switch (sec.locMode) {
        case ORIGINAL:
            // There can be no location information.
            return member;
        case PARAMETER:
        {
            // should have a "parameter = "
            int paramIdx = Integer.parseInt(
                    line.substring(
                    line.indexOf(paramIdxHead) + paramIdxHead.length()));
            nextLine();
            return ((AMethod<A>) member).parameters.vivify(paramIdx);
        }
        case EXTENDED:
            // should have a "target = "
            /*@NonNull*/ String targetTypeName =
                line.substring(line.indexOf("//") + "//".length());
            /*@NonNull*/ TargetType targetType = TargetType.valueOf(targetTypeName);
            nextLine();
            ATypeElement<A> subOuterType;
            AElement<A> subElement;
            switch (targetType) {
            case FIELD_GENERIC_OR_ARRAY:
            case METHOD_RETURN_GENERIC_OR_ARRAY:
                subOuterType = (ATypeElement<A>) member;
                break;
            case METHOD_RECEIVER:
                // A method receiver doesn't have inner types, so return the
                // receiver and bypass the targetType.isGeneric() part below.
                return ((AMethod<A>) member).receiver;
            case METHOD_PARAMETER_GENERIC_OR_ARRAY:
                int paramIdx = Integer.parseInt(
                        line.substring(
                        line.indexOf(paramIdxHead) + paramIdxHead.length()));
                nextLine();
                subOuterType = ((AMethod<A>) member).parameters.vivify(paramIdx);
                break;
            case LOCAL_VARIABLE:
            case LOCAL_VARIABLE_GENERIC_OR_ARRAY:
                int index, scopeStart, scopeLength;
                /*@NonNull*/ Matcher m = localLocRegex.matcher(line);
                m.matches();
                index = Integer.parseInt(m.group(1));
                scopeStart = Integer.parseInt(m.group(2));
                scopeLength = Integer.parseInt(m.group(3));
                /*@NonNull*/ LocalLocation ll =
                    new LocalLocation(index, scopeStart, scopeLength);
                nextLine();
                subOuterType = ((AMethod<A>) member).locals.vivify(ll);
                break;
            case TYPECAST:
            case TYPECAST_GENERIC_OR_ARRAY:
            {
                int offset = parseOffset();
                subOuterType = ((AMethod<A>) member).typecasts.vivify(offset);
                break;
            }
            case INSTANCEOF:
            case INSTANCEOF_GENERIC_OR_ARRAY:
            {
                int offset = parseOffset();
                subOuterType = ((AMethod<A>) member).instanceofs.vivify(offset);
                break;
            }
            case NEW:
            case NEW_GENERIC_OR_ARRAY:
            {
                int offset = parseOffset();
                subOuterType = ((AMethod<A>) member).news.vivify(offset);
                break;
            }
            // TEMP
            case UNKNOWN:
            default:
                throw new AssertionError();
            }
            if (targetType.isGeneric()) {
                /*@NonNull*/ List<Integer> location = parseInnerTypeLocationNums();
                InnerTypeLocation itl = new InnerTypeLocation(location);
                subElement = subOuterType.innerTypes.vivify(itl);
            } else
                subElement = subOuterType;
            return subElement;
        default:
            throw new AssertionError();
        }
    }
    
    private void parseAnnotationSection(/*@NonNull*/ AElement<A> member, /*@NonNull*/ AnnotationSection sec) throws IOException, ParseException {
        // FILL
        while (inData()) {
            /*@NonNull*/ String annoTypeName = parseAnnotationHead();
            AnnotationBuilder<A> ab = scene.af.beginAnnotation(annoTypeName);
            if (ab == null) {
                // don't care about the result
                // but need to skip over it anyway
                parseAnnotationBody(
                        SimpleAnnotationFactory.saf.beginAnnotation(annoTypeName),
                        SimpleAnnotationFactory.saf,
                        SECTION_DATA_PREFIX);
            } else {
                /*@NonNull*/ A a = parseAnnotationBody(ab, scene.af,
                        SECTION_DATA_PREFIX);
                // Wrap it in a TLA with the appropriate retention policy
                /*@NonNull*/ RetentionPolicy retention = sec.retention;
                /*@NonNull*/ TLAnnotation<A> tla = new TLAnnotation<A>(
                        new TLAnnotationDef(a.def(), retention), a);
                // Now we need to parse the location information to determine
                // which element gets the annotation.
                /*@NonNull*/ AElement<A> annoMember = chooseSubElement(member, sec);
                annoMember.tlAnnotationsHere.add(tla);
            }
        }
    }
    
    private void parseMember(/*@NonNull*/ AElement<A> member) throws IOException, ParseException {
        while (inMember()) {
            // New section
            /*@NonNull*/ String secTitle =
                line.substring(2, line.indexOf(':'));
            AnnotationSection sec0 = null;
            for (AnnotationSection s : AnnotationSection.values()) {
                if (s.secTitle.equals(secTitle)) {
                    sec0 = s;
                }
            }
            if (sec0 != null) {
                /*@NonNull*/ AnnotationSection sec = (/*@NonNull*/ AnnotationSection) sec0;
                nextLine();
                System.out.println("Got section " + secTitle);
                parseAnnotationSection(member, sec);
            } else {
                System.out.println("Got unrecognized section " + secTitle);
                nextLine();
                // Skip the section
                while (inData())
                    nextLine();
            }
        }
    }
    
    private void parseMethodBody(/*@NonNull*/ AElement<A> clazz, /*@NonNull*/ String methodName) throws IOException, ParseException {
        String sig = line.substring((SECTION_TITLE_PREFIX + "Signature: ").length());
        nextLine();
        String methodKey = methodName + sig;
        System.out.println("Got method " + methodKey); // TEMP
        parseMember(((AClass<A>) clazz).methods.vivify(methodKey));
    }
    
    // the "clazz" might actually be a package in case of "interface package-info"
    private void parseClass(/*@NonNull*/ AElement<A> clazz) throws IOException, ParseException {
        parseMember(clazz);
        
        nextLine(); // {
        
        while (!line.equals("}")) {
            // new member
            if (line.indexOf("static {}") >= 0) {
                nextLine();
                parseMethodBody(clazz, "<clinit>");
            } else {
                int lparen = line.indexOf('(');
                if (lparen == -1) {
                    // field
                    int space = line.lastIndexOf(' ');
                    /*@NonNull*/ String fieldName = line.substring(space + 1, line.length() - 1);
                    nextLine();
                    System.out.println("Got field " + fieldName); // TEMP
                    parseMember(((AClass<A>) clazz).fields.vivify(fieldName));
                } else {
                    // method
                    int space = line.lastIndexOf(' ', lparen);
                    /*@NonNull*/ String methodName = line.substring(space + 1, lparen);
                    nextLine();
                    parseMethodBody(clazz, methodName);
                }
            }
        }
        nextLine(); // }
    }

    private void parse() throws IOException, ParseException {
        try { // TEMP
        nextLine(); // get the first line
        
        while (line != null) {
            // new class
            nextLine();
            trim("public ");
            trim("protected ");
            trim("private ");
            trim("abstract ");
            trim("final ");
            trim("class ");
            trim("interface ");
            int nameEnd = line.indexOf(' ');
            String className = (nameEnd == -1) ? line
                    : line.substring(0, line.indexOf(' '));
            String pp = packagePart(className), bp = basenamePart(className);
            nextLine();
            if (bp.equals("package-info"))
                parseClass(scene.packages.vivify(pp));
            else
                parseClass(scene.classes.vivify(className));
        }
        } catch (RuntimeException e) {
            throw new RuntimeException("Line " + lineNo, e);
        }
    }

    private JavapParser(/*@NonNull*/ Reader in, /*@NonNull*/ AScene<A> scene) {
        bin = new BufferedReader(in);

        this.scene = scene;
    }
    
    /**
     * Transfers annotations from <code>in</code> to <code>scene</code>.
     */
    public static <A extends Annotation> void parse(/*@NonNull*/ Reader in, /*@NonNull*/
    AScene<A> scene) throws IOException, ParseException {
        new JavapParser<A>(in, scene).parse();
    }
    
    public static <A extends Annotation> void parse(/*@NonNull*/ String filename, /*@NonNull*/
    AScene<A> scene) throws IOException, FileParseException {
        try {
            parse(new FileReader(filename), scene);
        } catch (ParseException e) {
            throw new FileParseException(e, filename);
        }
    }
}
