package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.Base64EncodeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class Base64EncodeFilterTest {
    private Base64EncodeFilter base64EncodeFilter;

    @BeforeEach
    void setUp() {
        base64EncodeFilter = new Base64EncodeFilter();
    }

    @Test
    void testEncodeSimpleString() throws Exception {
        Object result = base64EncodeFilter.apply("hello", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("aGVsbG8=");
    }

    @Test
    void testEncodeCredentials() throws Exception {
        Object result = base64EncodeFilter.apply("user:password", new HashMap<>(), null, null, 0);
        String expected = Base64.getEncoder().encodeToString("user:password".getBytes());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testEncodeEmptyString() throws Exception {
        Object result = base64EncodeFilter.apply("", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("");
    }

    @Test
    void testEncodeNumber() throws Exception {
        Object result = base64EncodeFilter.apply(123, new HashMap<>(), null, null, 0);
        String expected = Base64.getEncoder().encodeToString("123".getBytes());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testNullInput() throws Exception {
        Object result = base64EncodeFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testArgumentNames() {
        assertThat(base64EncodeFilter.getArgumentNames()).isEmpty();
    }
}

