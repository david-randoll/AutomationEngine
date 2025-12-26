package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.Base64DecodeFilter;
import io.pebbletemplates.pebble.error.PebbleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base64DecodeFilterTest {
    private Base64DecodeFilter base64DecodeFilter;

    @BeforeEach
    void setUp() {
        base64DecodeFilter = new Base64DecodeFilter();
    }

    @Test
    void testDecodeSimpleString() throws Exception {
        Object result = base64DecodeFilter.apply("aGVsbG8=", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void testDecodeCredentials() throws Exception {
        Object result = base64DecodeFilter.apply("dXNlcjpwYXNzd29yZA==", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("user:password");
    }

    @Test
    void testDecodeEmptyString() throws Exception {
        Object result = base64DecodeFilter.apply("", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("");
    }

    @Test
    void testNullInput() throws Exception {
        Object result = base64DecodeFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testInvalidBase64ThrowsException() {
        assertThatThrownBy(() ->
            base64DecodeFilter.apply("invalid!@#", new HashMap<>(), null, null, 0)
        ).isInstanceOf(PebbleException.class);
    }

    @Test
    void testNonStringInputThrowsException() {
        assertThatThrownBy(() ->
            base64DecodeFilter.apply(123, new HashMap<>(), null, null, 0)
        ).isInstanceOf(PebbleException.class);
    }

    @Test
    void testArgumentNames() {
        assertThat(base64DecodeFilter.getArgumentNames()).isEmpty();
    }
}

