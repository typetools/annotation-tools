package annotations.tests;

import java.util.*; 

@interface AClass {}
@interface A00 {}
@interface A01 {}
@interface A02 {}
@interface A04 {}
@interface A05 {}
@interface A06 {}
@interface A08 {}
@interface A09 {}
@interface A0A {}
@interface A0B {}
@interface A0C {}
@interface A0D {}
@interface A0E {}
@interface A0F {}
@interface A10 {}
@interface A11 {}
@interface A12 {}
@interface A13 {}
@interface CClass {}
@interface C00 {}
@interface C01 {}
@interface C02 {}
@interface C04 {}
@interface C05 {}
@interface C06 {}
@interface C08 {}
@interface C09 {}
@interface C0A {}
@interface C0B {}
@interface C0C {}
@interface C0D {}
@interface C0E {}
@interface C0F {}
@interface C10 {}
@interface C11 {}
@interface C12 {}
@interface C13 {}

public @AClass /*@CClass*/ class AnnotationTest<Foo extends @A10 /*@C10*/ Comparable<@A11 /*@C11*/ Integer>> {
    
    @A0E /*@C0E*/ Iterable<@A0F /*@C0F*/ String> field;
    
    <Bar extends @A12 /*@C12*/ Comparable<@A13 /*@C13*/ Integer>> @A0A /*@C0A*/ HashSet<@A0B /*@C0B*/ Integer>
        doSomething(@A06 AnnotationTest this, @A0C /*@C0C*/ Set<@A0D /*@C0D*/ Integer> param) /*@C06*/ {
        @A08 /*@C08*/ HashSet<@A09 /*@C09*/ Integer> local;
        if (param instanceof @A02 /*@C02*/ HashSet)
            local = (@A00 /*@C00*/ HashSet<@A01 /*@C01*/ Integer>) param;
        else
            local = new @A04 /*@C04*/ HashSet<@A05 /*@C05*/ Integer>();
        return local;
    }
}
