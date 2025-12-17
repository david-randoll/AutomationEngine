package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.pebble.error.PebbleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FromJsonFilterTest {
    private FromJsonFilter fromJsonFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        fromJsonFilter = new FromJsonFilter(objectMapper);
    }

    @Test
    void testParseJsonObject() throws Exception {
        String input = "{\"name\":\"Alice\",\"age\":30}";
        Map<String, Object> args = new HashMap<>();

        Object result = fromJsonFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Map.class);
        Map<?, ?> map = (Map<?, ?>) result;
        assertThat(map.get("name")).isEqualTo("Alice");
        assertThat(map.get("age")).isEqualTo(30);
    }

    @Test
    void testParseJsonArray() throws Exception {
        String input = "[\"apple\",\"banana\",\"cherry\"]";
        Map<String, Object> args = new HashMap<>();

        Object result = fromJsonFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(List.class);
        List<?> list = (List<?>) result;
        assertThat(list).hasSize(3);
        assertThat(list.get(0)).isEqualTo("apple");
    }

    @Test
    void testParseJsonString() throws Exception {
        String input = "\"hello\"";
        Object result = fromJsonFilter.apply(input, new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void testParseJsonNumber() throws Exception {
        String input = "42";
        Object result = fromJsonFilter.apply(input, new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void testNullInput() throws Exception {
        Object result = fromJsonFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isNull();
    }

    @Test
    void testInvalidJsonThrowsException() {
        String input = "{invalid json}";
        assertThatThrownBy(() ->
            fromJsonFilter.apply(input, new HashMap<>(), null, null, 0)
        ).isInstanceOf(PebbleException.class);
    }

    @Test
    void testNonStringInputThrowsException() {
        assertThatThrownBy(() ->
            fromJsonFilter.apply(123, new HashMap<>(), null, null, 0)
        ).isInstanceOf(PebbleException.class);
    }

    @Test
    void testArgumentNames() {
        assertThat(fromJsonFilter.getArgumentNames()).isEmpty();
    }
}

