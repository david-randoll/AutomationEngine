package com.davidrandoll.automation.engine.templating.extensions.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class UrlEncodeFilterTest {
    private UrlEncodeFilter urlEncodeFilter;

    @BeforeEach
    void setUp() {
        urlEncodeFilter = new UrlEncodeFilter();
    }

    @Test
    void testEncodeSimpleString() throws Exception {
        Object result = urlEncodeFilter.apply("hello world", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello+world");
    }

    @Test
    void testEncodeSpecialCharacters() throws Exception {
        Object result = urlEncodeFilter.apply("hello@world.com", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello%40world.com");
    }

    @Test
    void testEncodeQueryParam() throws Exception {
        Object result = urlEncodeFilter.apply("search term with spaces", new HashMap<>(), null, null, 0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).doesNotContain(" ");
    }

    @Test
    void testEncodeAlreadyEncodedString() throws Exception {
        Object result = urlEncodeFilter.apply("hello%20world", new HashMap<>(), null, null, 0);
        // Should encode the % sign
        assertThat((String) result).contains("%25");
    }

    @Test
    void testEncodeNumber() throws Exception {
        Object result = urlEncodeFilter.apply(123, new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("123");
    }

    @Test
    void testNullInput() throws Exception {
        Object result = urlEncodeFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testArgumentNames() {
        assertThat(urlEncodeFilter.getArgumentNames()).isEmpty();
    }
}

