package annotator.specification;

import java.io.*;
import java.util.*;

import annotator.find.*;

import com.sun.source.tree.Tree;

/**
 * Represents an annotation specification file as produced by Javari.
 */
public class JavariOutputSpecification implements Specification {

    private byte[] spec;
    private int bp = 0;
    private StringBuilder buf;

    private Queue<String> tokens;
    // private final Properties keywords;
    private List<Insertion> insertion;

    /**
     * Creates a new {@code JavariOutputSpecification}.
     *
     * @throws FileNotFoundException if an input file couldn't be found
     * @throws IOException if an input file couldn't be read
     */
    public JavariOutputSpecification(String filename)
            throws FileNotFoundException, IOException {

        this.buf = new StringBuilder();
        this.tokens = new LinkedList<String>();
        this.insertion = new LinkedList<Insertion>();

        // this.keywords = new Properties();
        // keywords.load(new FileInputStream(options.getOptionValue("k")));

        FileInputStream in = new FileInputStream(filename);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1)
            bytes.write(c);

        this.spec = bytes.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    public List<Insertion> parse() {

        // Scan the entire thing into a buffer for tokens -- probably
        // not the best way to do this.
        String s;
        do {
            s = scan();
            if (s == null)
                break;
            tokens.add(s);
        } while (s != null);

        // Begin parsing.
        parseClass();

        // TODO: jaime
        System.out.println("Insertions: ");
        for (Insertion i : this.insertion) {
          System.out.println(i);
        }
        return this.insertion;
    }

    /* ---------- Scanner Methods ---------- */

    /**
     * @return the next character from the buffer
     */
    private final char nextChar() {
        if (++bp >= spec.length) return 0;
            return (char)spec[bp];
    }

    /**
     * @return the next token from the buffer
     */
    private String scan() {

        if (bp >= spec.length)
            return null;

        // Clear the buffer.
        buf = new StringBuilder();

        char c = (char)spec[bp];
        while (Character.isWhitespace(c))
            c = nextChar();

        if (c == '{' || c == '(' || c == '}' || c == ')' || c == ':' || c == ',') {
            String g = Character.toString(c);
            c = nextChar();
            return g;
        }

        while (bp < spec.length) {

            if (Character.isWhitespace(c))
                break;
            else if (c == '{' || c == '(' || c == '}' || c == ')' || c == ':' || c == ',')
                break;
            else {
                buf.append(c);
                c = nextChar();
            }
        }

        return buf.toString();

    }

    /* ---------- Parser Methods ---------- */

    /**
     * Verify that the next token is the given one.
     */
    private void accept(String token) {
        String s = tokens.poll();
        if (!s.equals(token))
            throw new RuntimeException("expected: " + token + "; got " + s);
    }

    /**
     * @param amount the number of tokens to look ahead
     * @return the token {@code amount} tokens after the next
     */
    private String lookahead(int amount) {
        if (amount > tokens.size())
            return null;
        int c = 0;
        Iterator<String> i = tokens.iterator();
        while (c++ < amount)
            i.next();
        return i.next();
    }

    private void parseClass() {
        String clazz = tokens.poll();
        if (clazz.contains("<"))
            clazz = clazz.substring(0, clazz.indexOf('<'));
        accept("{");
        parseVariables(clazz, null);
        parseMethods(clazz);
        accept("}");
    }

    private void parseVariables(String clazz, String method) {
        while (lookahead(1).equals(":"))
            parseVariable(clazz, method);
    }

    private void parseVariable(String clazz, String method) {
        String name = tokens.poll();
        String modifier = null;

        accept(":");

        // if (keywords.containsKey(tokens.peek()))
        //     modifier = keywords.getProperty(tokens.poll());

        // Parse the type.
        tokens.poll();

        if (modifier != null && !name.startsWith("$e")) {
            Criteria c = new Criteria();
            c.add(Criteria.is(Tree.Kind.VARIABLE, name));
            c.add(Criteria.inClass(clazz, /*exactMatch=*/ true));
            if (method != null)
                c.add(Criteria.inMethod(method));
            else c.add(Criteria.notInMethod());
            insertion.add(new Insertion(modifier, c, false));
        }
    }

    private void parseMethods(String clazz) {
        while (!lookahead(0).equals("}"))
            parseMethod(clazz);
    }

    private void parseMethod(String clazz) {
        String modifier = null;
        // if (keywords.containsKey(lookahead(0)))
        //     modifier = keywords.getProperty(tokens.poll());

        // Parse the type.
        tokens.poll();

        String method = tokens.poll();

        if (modifier != null) {
            Criteria c = new Criteria();
            c.add(Criteria.is(Tree.Kind.METHOD, method));
            c.add(Criteria.inClass(clazz, /*exactMatch=*/ true));
            insertion.add(new Insertion(modifier, c, false));
        }

        accept("(");
        parseMethodParameters(clazz, method);
        accept(")");
        parseMethodReceiver(clazz, method);
        accept("{");
        parseMethodLocals(clazz, method);
        accept("}");
    }

    private void parseMethodParameters(String clazz, String method) {
      System.out.println("clazz: " + clazz + ", method: " + method);
        if (lookahead(0).equals(")"))
            return;

        while (!lookahead(0).equals(")")) {

            String modifier = null;

            // if (keywords.containsKey(lookahead(0)))
            //     modifier = keywords.getProperty(tokens.poll());

            tokens.poll(); // type

            String name = tokens.poll();

            if (!lookahead(0).equals(")"))
                accept(",");

            System.out.println("modifier: " + modifier);
            if (modifier != null) {
                Criteria c = new Criteria();
                c.add(Criteria.is(Tree.Kind.VARIABLE, name));
                c.add(Criteria.inMethod(method));
                c.add(Criteria.inClass(clazz, /*exactMatch=*/ true));
                insertion.add(new Insertion(modifier, c, false));
                System.out.println("added var");
            }
        }
    }

    private void parseMethodReceiver(String clazz, String method) {

        if (lookahead(0).equals("{"))
            return;

        tokens.poll(); // modifier
        tokens.poll(); //type

        // XTODO handle this case?
    }

    private void parseMethodLocals(String clazz, String method) {
        parseVariables(clazz, method);
    }
}
