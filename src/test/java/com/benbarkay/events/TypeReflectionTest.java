package com.benbarkay.events;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TypeReflectionTest {

    interface A {

    }

    interface B {

    }

    interface C extends B, A {

    }

    interface D extends C, A { // because A is also a type from C, it should be after C

    }

    @Test
    public void test() {
        List<Class> expected = Arrays.asList(
                D.class,
                C.class,
                B.class,
                A.class
        );

        assertEquals(expected, new TypeReflection(D.class)
                .getAssignableTypes());
    }

}