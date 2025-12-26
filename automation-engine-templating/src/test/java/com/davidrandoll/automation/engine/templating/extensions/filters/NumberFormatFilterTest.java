package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.NumberFormatFilter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NumberFormatFilterTest {

    private final NumberFormatFilter filter = new NumberFormatFilter();

    @Test
    void testGetArgumentNames() {
        assertThat(filter.getArgumentNames()).containsExactly("decimalPlaces");
    }

    @Test
    void testApplyWithDefaultDecimalPlaces() throws Exception {
        Object result = filter.apply(99.999, Map.of(), null, null, 0);
        assertThat(result).isEqualTo("100.00");
    }

    @Test
    void testApplyWithCustomDecimalPlaces() throws Exception {
        Object result = filter.apply(99.456, Map.of("decimalPlaces", 3), null, null, 0);
        assertThat(result).isEqualTo("99.456");
    }

    @Test
    void testApplyWithZeroDecimalPlaces() throws Exception {
        Object result = filter.apply(42.7, Map.of("decimalPlaces", 0), null, null, 0);
        assertThat(result).isEqualTo("43");
    }

    @Test
    void testApplyWithInteger() throws Exception {
        Object result = filter.apply(100, Map.of(), null, null, 0);
        assertThat(result).isEqualTo("100.00");
    }

    @Test
    void testApplyWithNonNumber() throws Exception {
        String input = "not a number";
        Object result = filter.apply(input, Map.of(), null, null, 0);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void testApplyWithOneDecimalPlace() throws Exception {
        Object result = filter.apply(1234.5678, Map.of("decimalPlaces", 1), null, null, 0);
        assertThat(result).isEqualTo("1234.6");
    }
}

