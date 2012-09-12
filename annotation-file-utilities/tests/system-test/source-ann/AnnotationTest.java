package annotations.tests;

import java.util.*;
import java.lang.annotation.*;

@interface AClass {}
@Target(ElementType.TYPE_USE)
@interface A00 {}
@Target(ElementType.TYPE_USE)
@interface A01 {}
@Target(ElementType.TYPE_USE)
@interface A02 {}
@Target(ElementType.TYPE_USE)
@interface A04 {}
@Target(ElementType.TYPE_USE)
@interface A05 {}

@Target({ElementType.TYPE_USE, ElementType.PARAMETER})
@interface A06 {}
@interface A08 {}
@Target(ElementType.TYPE_USE)
@interface A09 {}

@Target({ElementType.TYPE_USE, ElementType.METHOD})
@interface A0A {}
@Target(ElementType.TYPE_USE)
@interface A0AT {}
@Target(ElementType.TYPE_USE)
@interface A0B {}

@Target({ElementType.TYPE_USE, ElementType.PARAMETER})
@interface A0C {}
@Target(ElementType.TYPE_USE)
@interface A0D {}

@Target({ElementType.TYPE_USE, ElementType.FIELD})
@interface A0E {}


@Target(ElementType.TYPE_USE)
@interface A0F {}
@interface A10 {}
@interface A11 {}
@interface A12 {}
@interface A13 {}
@interface CClass {}
@Target(ElementType.TYPE_USE)
@interface C00 {}
@Target(ElementType.TYPE_USE)
@interface C01 {}
@Target(ElementType.TYPE_USE)
@interface C02 {}
@Target(ElementType.TYPE_USE)
@interface C04 {}
@Target(ElementType.TYPE_USE)
@interface C05 {}

@Target({ElementType.TYPE_USE, ElementType.PARAMETER})
@interface C06 {}
@interface C08 {}
@Target(ElementType.TYPE_USE)
@interface C09 {}
@Target({ElementType.TYPE_USE, ElementType.METHOD})
@interface C0A {}
@Target(ElementType.TYPE_USE)
@interface C0AT {}
@Target(ElementType.TYPE_USE)
@interface C0B {}

@Target({ElementType.TYPE_USE, ElementType.PARAMETER})
@interface C0C {}
@Target(ElementType.TYPE_USE)
@interface C0D {}
@Target({ElementType.TYPE_USE, ElementType.FIELD})
@interface C0E {}
@Target(ElementType.TYPE_USE)
@interface C0F {}
@interface C10 {}
@interface C11 {}
@interface C12 {}
@interface C13 {}

public @AClass /*@CClass*/ class AnnotationTest<Foo extends /*A10*/ /*C10*/ Comparable</*A11*/ /*C11*/ Integer>> {

    @A0E /*@C0E*/ Iterable<@A0F /*@C0F*/ String> field;

    // TODO: crash when A12, C12, A13, or C13 are annotations!
    <Bar extends /*A12*/ /*C12*/ Comparable</*A13*/ /*C13*/ Integer>> @A0A /*@C0A*/ @A0AT /*@C0AT*/ HashSet<@A0B /*@C0B*/ Integer>
        doSomething(@A06 /*@C06*/ AnnotationTest<Foo> this, @A0C /*@C0C*/ Set<@A0D /*@C0D*/ Integer> param) {
        @A08 /*@C08*/ HashSet<@A09 /*@C09*/ Integer> local;
        if (param instanceof @A02 /*@C02*/ HashSet)
            local = (@A00 /*@C00*/ HashSet<@A01 /*@C01*/ Integer>) param;
        else
            local = new @A04 /*@C04*/ HashSet<@A05 /*@C05*/ Integer>();
        return local;
    }
}
