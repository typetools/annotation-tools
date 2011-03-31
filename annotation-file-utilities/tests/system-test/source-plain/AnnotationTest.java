package annotations.tests;

import java.util.*; 

public class AnnotationTest<Foo extends Comparable<Integer>> {
    
    Iterable<String> field;
    
    <Bar extends Comparable<Integer>> HashSet<Integer>
        doSomething(Set<Integer> param) {
        HashSet<Integer> local;
        if (param instanceof HashSet)
            local = (HashSet<Integer>) param;
        else
            local = new HashSet<Integer>();
        return local;
    }
}
