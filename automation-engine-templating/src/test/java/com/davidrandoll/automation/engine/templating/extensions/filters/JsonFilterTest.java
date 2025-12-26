package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.pebbles.extensions.filters.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonFilterTest {
    private JsonFilter jsonFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jsonFilter = new JsonFilter(objectMapper);
    }

    @Test
    void testSimpleObjectToJson() throws Exception {
        Map<String, Object> input = Map.of("name", "Alice", "age", 30);
        Map<String, Object> args = new HashMap<>();

        Object result = jsonFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("\"name\":\"Alice\"");
        assertThat((String) result).contains("\"age\":30");
    }

    @Test
    void testPrettyPrintJson() throws Exception {
        Map<String, Object> input = Map.of("name", "Bob");
        Map<String, Object> args = Map.of("pretty", true);

        Object result = jsonFilter.apply(input, args, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat((String) result).contains("\n"); // Pretty print includes newlines
    }

    @Test
    void testNullInput() throws Exception {
        Object result = jsonFilter.apply(null, new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("null");
    }

    @Test
    void testStringInput() throws Exception {
        Object result = jsonFilter.apply("test", new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("\"test\"");
    }

    @Test
    void testNumberInput() throws Exception {
        Object result = jsonFilter.apply(123, new HashMap<>(), null, null, 0);
        assertThat(result).isEqualTo("123");
    }

    @Test
    void testArgumentNames() {
        assertThat(jsonFilter.getArgumentNames()).hasSize(1);
        assertThat(jsonFilter.getArgumentNames()).contains("pretty");
    }
}


