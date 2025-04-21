package com.automation.engine.http.utils;

import com.automation.engine.http.exceptions.AutomationEngineInvalidRegexException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@UtilityClass
public class JsonNodeMatcher {

    public static boolean matches(Object expectedObj, Object actualObj, ObjectMapper mapper) {
        JsonNode expectedNode = toJsonNode(expectedObj, mapper);
        JsonNode actualNode = toJsonNode(actualObj, mapper);
        return !matchesNode(expectedNode, actualNode);
    }

    static JsonNode toJsonNode(Object obj, ObjectMapper mapper) {
        return obj instanceof JsonNode node ? node : mapper.convertValue(obj, JsonNode.class);
    }

    static boolean matchesNode(JsonNode expected, JsonNode actual) {
        if (isNull(expected)) return true;
        if (isNull(actual)) return false;

        if (expected.isObject()) {
            return matchesObject(expected, actual);
        }

        if (expected.isArray()) {
            return matchesArray(expected, actual);
        }

        String expectedText = expected.asText().toLowerCase().trim();
        String actualText = actual.asText().toLowerCase().trim();
        try {
            return Pattern.compile(expectedText).matcher(actualText).matches();
        } catch (PatternSyntaxException e) {
            throw new AutomationEngineInvalidRegexException(expectedText, e);
        }
    }

    private static boolean matchesObject(JsonNode expected, JsonNode actual) {
        for (Map.Entry<String, JsonNode> expectedField : iterable(expected.fields())) {
            JsonNode actualValue = getFieldIgnoreCase(actual, expectedField.getKey());
            if (!matchesNode(expectedField.getValue(), actualValue)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesArray(JsonNode expected, JsonNode actual) {
        if (expected.isEmpty() && actual.isEmpty()) return true;
        if (!actual.isArray()) {
            for (JsonNode expectedElement : expected) {
                if (matchesNode(expectedElement, actual)) return true;
            }
            return false;
        }

        for (JsonNode expectedElement : expected) {
            for (JsonNode actualElement : actual) {
                if (matchesNode(expectedElement, actualElement)) return true;
            }
        }
        return false;
    }

    private static boolean isNull(JsonNode node) {
        return node == null || node.isNull();
    }

    private static JsonNode getFieldIgnoreCase(JsonNode node, String key) {
        for (Map.Entry<String, JsonNode> field : iterable(node.fields())) {
            if (field.getKey().trim().equalsIgnoreCase(key.trim())) {
                return field.getValue();
            }
        }
        return null;
    }

    private static <T> Iterable<T> iterable(Iterator<T> iterator) {
        return () -> iterator;
    }
}