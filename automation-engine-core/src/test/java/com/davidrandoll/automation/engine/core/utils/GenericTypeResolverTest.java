package com.davidrandoll.automation.engine.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericTypeResolverTest {

    static class StringGeneric extends BaseGeneric<String> {
    }

    static class BaseGeneric<T> {
    }

    static class NoGeneric {
    }

    @Test
    void testGetGenericParameterClass() {
        Class<?> clazz = GenericTypeResolver.getGenericParameterClass(StringGeneric.class);
        assertEquals(String.class, clazz);
    }

    @Test
    void testGetGenericParameterClassNull() {
        assertThrows(IllegalArgumentException.class, () -> GenericTypeResolver.getGenericParameterClass(null));
    }

    @Test
    void testGetGenericParameterClassNoGeneric() {
        assertThrows(RuntimeException.class, () -> GenericTypeResolver.getGenericParameterClass(NoGeneric.class));
    }
}
