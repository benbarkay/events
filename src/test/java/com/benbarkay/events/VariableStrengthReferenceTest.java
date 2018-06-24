package com.benbarkay.events;

import org.junit.Test;

import static org.junit.Assert.*;

public class VariableStrengthReferenceTest {

    @Test
    public void isGarbageCollectedReturnsFalseWhenObjectIsNotGarbageCollected() {
        Object object = new Object();
        VariableStrengthReference<Object> reference = new VariableStrengthReference<>(object);
        reference.setStrong(false);

        System.gc();

        assertFalse(reference.isGarbageCollected());
    }

    @Test
    public void isGarbageCollectedReturnsTrueWhenObjectIsGarbageCollected() {
        VariableStrengthReference<Object> reference = new VariableStrengthReference<>(new Object());
        reference.setStrong(false);

        System.gc();

        assertTrue(reference.isGarbageCollected());
    }

    @Test
    public void itRefersToObjectStronglyWhenSetStrongToTrue() {
        VariableStrengthReference<Object> reference = new VariableStrengthReference<>(new Object());

        System.gc();

        assertNotNull(reference.value());
    }

    @Test
    public void valueReturnsTrueWhenObjectIsNotGarbageCollected() {
        Object expected = new Object();
        VariableStrengthReference<Object> reference = new VariableStrengthReference<>(expected);

        System.gc();

        assertEquals(expected, reference.value());
    }

    @Test
    public void valueReturnsNullWhenObjectIsGarbageCollected() {
        VariableStrengthReference<Object> reference = new VariableStrengthReference<>(new Object());
        reference.setStrong(false);
        System.gc();

        assertNull(reference.value());
    }
}