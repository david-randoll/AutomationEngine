package com.automation.engine.http.utils;

import com.automation.engine.http.exceptions.AutomationEngineInvalidRegexException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonNodeMatcherTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void testExactMatchObjects() {
        var expected = """
                    { "name": "Alice", "age": 30 }
                """;
        var actual = """
                    { "name": "Alice", "age": 30 }
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testMismatchValue() {
        var expected = """
                    { "name": "Alice", "age": 30 }
                """;
        var actual = """
                    { "name": "Alice", "age": 31 }
                """;

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testRegexMatch() {
        var expected = """
                    { "email": ".*@example.com" }
                """;
        var actual = """
                    { "email": "test@EXAMPLE.com" }
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testRegexDoesNotMatch() {
        var expected = """
                    { "email": ".*@example.com" }
                """;
        var actual = """
                    { "email": "test@gmail.com" }
                """;

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testNullExpectedAlwaysMatches() {
        assertFalse(JsonNodeMatcher.matches(null, Map.of("key", "value"), mapper));
    }

    @Test
    void testNullActualFails() {
        assertTrue(JsonNodeMatcher.matches(Map.of("key", "value"), null, mapper));
    }

    @Test
    void testObjectFieldCaseInsensitive() {
        var expected = """
                    { "NaMe": "Alice" }
                """;
        var actual = """
                    { "name": "Alice" }
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testArrayElementMatch() {
        var expected = """
                    [ { "type": "fruit" } ]
                """;
        var actual = """
                    [ { "type": "fruit" }, { "type": "vegetable" } ]
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testExpectedArrayActualSingleValue() {
        var expected = """
                    [ "hello" ]
                """;
        var actual = """
                    "hello"
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testExpectedArrayActualSingleValueFails() {
        var expected = """
                    [ "hello" ]
                """;
        var actual = """
                    "goodbye"
                """;

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testExtraFieldsInActualAreIgnored() {
        var expected = """
                    { "name": "Alice" }
                """;
        var actual = """
                    { "name": "Alice", "age": 25 }
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testEmptyExpectedMatchesAnything() {
        assertFalse(JsonNodeMatcher.matches(Map.of(), Map.of("some", "data"), mapper));
    }

    @Test
    void testDeepNestedObjectMatch() {
        var expected = """
                    { "person": { "details": { "name": ".*ice" } } }
                """;
        var actual = """
                    { "person": { "details": { "name": "Alice" } } }
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testMismatchedTypesObjectVsArray() {
        var expected = """
                    { "data": { "name": "Alice" } }
                """;
        var actual = """
                    { "data": [ { "name": "Alice" } ] }
                """;

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testMalformedRegexInExpected() {
        var expected = """
                    { "name": "*invalid[" }
                """;
        var actual = """
                    { "name": "anything" }
                """;

        assertThrows(AutomationEngineInvalidRegexException.class, () ->
                JsonNodeMatcher.matches(json(expected), json(actual), mapper)
        );
    }

    @Test
    void testEmptyArrayExpectedMatchesEmptyArrayActual() {
        var expected = "[]";
        var actual = "[]";

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testEmptyArrayExpectedMatchesEmptyObjectActual() {
        var expected = "[]";
        var actual = "{}";

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testEmptyArrayExpectedMatchesEmptyStringActual() {
        var expected = "[]";
        var actual = "\"\"";

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testEmptyArrayExpectedMatchesNonEmptyArrayActual() {
        var expected = "[]";
        var actual = """
                ["value"]""";

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testTypeMismatchStringVsObject() {
        var expected = """
                    { "data": "simple string" }
                """;
        var actual = """
                    { "data": { "nested": "object" } }
                """;

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testLargeJsonPerformance() {
        StringBuilder expected = new StringBuilder("{");
        StringBuilder actual = new StringBuilder("{");

        for (int i = 0; i < 1000; i++) {
            expected.append("\"key").append(i).append("\":\"value").append(i).append("\",");
            actual.append("\"key").append(i).append("\":\"value").append(i).append("\",");
        }

        expected.append("\"last\":\"end\"}");
        actual.append("\"last\":\"end\"}");

        assertFalse(JsonNodeMatcher.matches(json(expected.toString()), json(actual.toString()), mapper));
    }

    @Test
    void testNumericMatchingWithDifferentTypes() {
        var expected = """
                    { "value": 5 }
                """;
        var actual = """
                    { "value": "5" }
                """;

        // Both are coerced to string via `asText().toLowerCase().trim()`, so should match
        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testArrayWithDuplicateElements() {
        var expected = """
                    [ "a", "a" ]
                """;
        var actual = """
                    [ "a" ]
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testFieldPresentWithNullValue() {
        var expected = """
                    { "name": null }
                """;
        var actual = """
                    { "name": null }
                """;

        // expected null matches anything, so match succeeds
        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testFieldMissingInActualShouldFail() {
        var expected = """
                    { "important": ".*" }
                """;
        var actual = """
                    { }
                """;

        assertTrue(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testExpectedHasNestedArrayOfObjects() {
        var expected = """
                    { "items": [ { "type": "fruit" }, { "type": "veg" } ] }
                """;
        var actual = """
                    { "items": [ { "type": "veg" }, { "type": "fruit" } ] }
                """;

        // Order-independent matching, so this should pass
        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    private JsonNode json(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }

    @Test
    void testRegexLooksLikeLiteralButMatchesUnexpectedly() {
        var expected = """
                { "code": ".*" }""";
        var actual = """
                { "code": ".*" }""";

        // Should match literally? But right now it matches everything.
        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testExpectedNullActualMissing() {
        var expected = """
                { "optional": null }""";
        var actual = """
                { }""";

        // Should match, since expected is null (wildcard)
        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testDeeplyNestedObjectMatch() {
        var expected = """
                    {
                      "a": {
                        "b": {
                          "c": {
                            "d": "deep"
                          }
                        }
                      }
                    }
                """;
        var actual = """
                    {
                      "a": {
                        "b": {
                          "c": {
                            "d": "deep"
                          }
                        }
                      }
                    }
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testMixedArrayMatching() {
        var expected = """
                    [1, "two", { "x": ".*" }, null]
                """;
        var actual = """
                    [1, "two", { "x": "value" }, null]
                """;

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testWhitespaceInKeyNames() {
        var expected = """
                { " name ": "John" }""";
        var actual = """
                { "name": "john" }""";

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testOrderSensitiveArrays() {
        var expected = """
                [ "a", "b", "c" ]""";
        var actual = """
                [ "c", "b", "a" ]""";

        // Should pass if order doesn’t matter, else fail
        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }

    @Test
    void testBooleanVsStringMatch() {
        var expected = """
                { "enabled": true }""";
        var actual = """
                { "enabled": "true" }""";

        assertFalse(JsonNodeMatcher.matches(json(expected), json(actual), mapper));
    }


}
