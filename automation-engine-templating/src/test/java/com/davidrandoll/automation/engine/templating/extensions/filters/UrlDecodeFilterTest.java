package com.davidrandoll.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlDecodeFilterTest {
    private UrlDecodeFilter urlDecodeFilter;

    @BeforeEach
    void setUp() {
        urlDecodeFilter = new UrlDecodeFilter();
    }

    @Test
    void testDecodeSimpleString() throws Exception {
        Object result = urlDecodeFilter.apply("hello+world", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void testDecodeSpecialCharacters() throws Exception {
        Object result = urlDecodeFilter.apply("hello%40world.com", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello@world.com");
    }

    @Test
    void testDecodeQueryParam() throws Exception {
        Object result = urlDecodeFilter.apply("search+term+with+spaces", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("search term with spaces");
    }

    @Test
    void testDecodePercentEncoded() throws Exception {
        Object result = urlDecodeFilter.apply("hello%20world", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void testDecodeAlreadyDecodedString() throws Exception {
        Object result = urlDecodeFilter.apply("hello world", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void testNullInput() throws Exception {
        Object result = urlDecodeFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testNonStringInputThrowsException() {
        assertThatThrownBy(() ->
            urlDecodeFilter.apply(123, new HashMap<>(), null, null, 0)
        ).isInstanceOf(PebbleException.class);
    }

    @Test
    void testArgumentNames() {
        assertThat(urlDecodeFilter.getArgumentNames()).isEmpty();
    }
}

