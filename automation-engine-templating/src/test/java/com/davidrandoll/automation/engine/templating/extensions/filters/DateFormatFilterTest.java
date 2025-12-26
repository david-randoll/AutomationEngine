package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.DateFormatFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DateFormatFilterTest {
    private DateFormatFilter dateFormatFilter;

    @BeforeEach
    void setUp() {
        dateFormatFilter = new DateFormatFilter();
    }

    @Test
    void testFormatLocalDateTime() throws Exception {
        LocalDateTime input = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        Map<String, Object> args = Map.of("pattern", "yyyy-MM-dd HH:mm:ss");

        Object result = dateFormatFilter.apply(input, args, null, null, 0);

        assertThat(result).isEqualTo("2023-12-25 14:30:45");
    }

    @Test
    void testFormatLocalDate() throws Exception {
        LocalDate input = LocalDate.of(2023, 12, 25);
        Map<String, Object> args = Map.of("pattern", "yyyy-MM-dd");

        Object result = dateFormatFilter.apply(input, args, null, null, 0);

        assertThat(result).isEqualTo("2023-12-25");
    }

    @Test
    void testDefaultPattern() throws Exception {
        LocalDateTime input = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        Map<String, Object> args = new HashMap<>();

        Object result = dateFormatFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("2023-12-25");
    }

    @Test
    void testFormatInstant() throws Exception {
        Instant input = Instant.parse("2023-12-25T14:30:45Z");
        Map<String, Object> args = Map.of("pattern", "yyyy-MM-dd");

        Object result = dateFormatFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("2023-12-25");
    }

    @Test
    void testFormatDate() throws Exception {
        Date input = Date.from(Instant.parse("2023-12-25T14:30:45Z"));
        Map<String, Object> args = Map.of("pattern", "yyyy-MM-dd");

        Object result = dateFormatFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("2023-12-25");
    }

    @Test
    void testNullInput() throws Exception {
        Object result = dateFormatFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testNonDateInput() throws Exception {
        Object result = dateFormatFilter.apply("not a date", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("not a date");
    }

    @Test
    void testArgumentNames() {
        assertThat(dateFormatFilter.getArgumentNames()).hasSize(1);
        assertThat(dateFormatFilter.getArgumentNames()).contains("pattern");
    }
}

