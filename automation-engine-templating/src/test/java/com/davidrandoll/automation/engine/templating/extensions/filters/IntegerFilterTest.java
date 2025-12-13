package com.davidrandoll.automation.engine.templating.extensions.filters;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegerFilterTest {

    private final IntegerFilter filter = new IntegerFilter();

    @Test
    void testGetArgumentNames() {
        assertThat(filter.getArgumentNames()).isEmpty();
    }

    @Test
    void testApplyWithNull() throws Exception {
        Object result = filter.apply(null, Map.of(), null, null, 0);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testApplyWithDouble() throws Exception {
        Object result = filter.apply(42.7, Map.of(), null, null, 0);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void testApplyWithInteger() throws Exception {
        Object result = filter.apply(99, Map.of(), null, null, 0);
        assertThat(result).isEqualTo(99);
    }

    @Test
    void testApplyWithString() throws Exception {
        Object result = filter.apply("123", Map.of(), null, null, 0);
        assertThat(result).isEqualTo(123);
    }

    @Test
    void testApplyWithStringWithWhitespace() throws Exception {
        Object result = filter.apply("  456  ", Map.of(), null, null, 0);
        assertThat(result).isEqualTo(456);
    }

    @Test
    void testApplyWithInvalidString() {
        assertThatThrownBy(() -> filter.apply("abc", Map.of(), null, null, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot convert to int");
    }
}

