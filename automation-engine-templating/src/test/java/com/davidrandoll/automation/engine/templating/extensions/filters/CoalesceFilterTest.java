package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.CoalesceFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoalesceFilterTest {
    private CoalesceFilter coalesceFilter;

    @BeforeEach
    void setUp() {
        coalesceFilter = new CoalesceFilter();
    }

    @Test
    void testInputIsNotNull() throws Exception {
        Object result = coalesceFilter.apply("first", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testInputNullWithOneArg() throws Exception {
        Map<String, Object> args = Map.of("0", "default");
        Object result = coalesceFilter.apply(null, args, null, null, 0);
        assertThat(result).isEqualTo("default");
    }

    @Test
    void testInputNullWithMultipleArgs() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("0", null);
        args.put("1", "second");
        args.put("2", "third");

        Object result = coalesceFilter.apply(null, args, null, null, 0);
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testAllNullReturnsNull() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("0", null);
        args.put("1", null);

        Object result = coalesceFilter.apply(null, args, null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testEmptyArgsReturnsNull() throws Exception {
        Object result = coalesceFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testZeroIsNotNull() throws Exception {
        Map<String, Object> args = Map.of("0", "fallback");
        Object result = coalesceFilter.apply(0, args, null, null, 0);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testEmptyStringIsNotNull() throws Exception {
        Map<String, Object> args = Map.of("0", "fallback");
        Object result = coalesceFilter.apply("", args, null, null, 0);
        assertThat(result).isEqualTo("");
    }

    @Test
    void testArgumentNames() {
        assertThat(coalesceFilter.getArgumentNames()).isNull();
    }
}

