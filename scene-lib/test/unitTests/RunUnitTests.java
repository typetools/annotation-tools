package unitTests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import scenelib.annotations.el.ATypeElementWithType;
import scenelib.type.ArrayType;
import scenelib.type.DeclaredType;

public class RunUnitTests extends TestCase {

    public static void main(String[] args) {
        TestSuite suite = new TestSuite(RunUnitTests.class);
        TestResult result = new TestResult();
        suite.run(result);
        System.out.println(
                "AnnotationsTests ran with "
                        + result.failureCount()
                        + " failures and "
                        + result.errorCount()
                        + " errors. ("
                        + result.runCount()
                        + " successes.)");
    }

    public void testEquality() {
        TestATypeElementWithTypeEquality();
    }

    private void TestATypeElementWithTypeEquality() {
        Constructor<ATypeElementWithType> m = null;
        try {
            m = ATypeElementWithType.class.getDeclaredConstructor(new Class[] {Object.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        m.setAccessible(true);
        ATypeElementWithType a = null;
        try {
            a = m.newInstance("hello");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        ATypeElementWithType e = a.clone();
        assertTrue(a.equals(e));
        a.setType(new ArrayType(new DeclaredType("hello")));
        e.setType(new DeclaredType("hello"));
        assertFalse(a.equals(e));
    }
}
