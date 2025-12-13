package com.davidrandoll.automation.engine.templating.extensions.filters;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TimeFormatFilterTest {

    private final TimeFormatFilter filter = new TimeFormatFilter();

    @Test
    void testGetArgumentNames() {
        assertThat(filter.getArgumentNames()).containsExactly("pattern");
    }

    @Test
    void testApplyWithLocalTimeAndDefaultPattern() throws Exception {
        LocalTime time = LocalTime.of(14, 30);
        Map<String, Object> args = Map.of();

        Object result = filter.apply(time, args, null, null, 0);

        assertThat(result).isEqualTo("02:30 PM");
    }

    @Test
    void testApplyWithLocalTimeAndCustomPattern() throws Exception {
        LocalTime time = LocalTime.of(9, 15);
        Map<String, Object> args = Map.of("pattern", "HH:mm:ss");

        Object result = filter.apply(time, args, null, null, 0);

        assertThat(result).isEqualTo("09:15:00");
    }

    @Test
    void testApplyWithStringAndDefaultPattern() throws Exception {
        String time = "14:30";
        Map<String, Object> args = Map.of();

        Object result = filter.apply(time, args, null, null, 0);

        assertThat(result).isEqualTo("02:30 PM");
    }

    @Test
    void testApplyWithStringAndCustomPattern() throws Exception {
        String time = "15:45";
        Map<String, Object> args = Map.of("pattern", "hh:mm a");

        Object result = filter.apply(time, args, null, null, 0);

        assertThat(result).isEqualTo("03:45 PM");
    }

    @Test
    void testApplyWithNonTimeObject() throws Exception {
        Integer number = 42;
        Map<String, Object> args = Map.of();

        Object result = filter.apply(number, args, null, null, 0);

        assertThat(result).isEqualTo(42);
    }
}

