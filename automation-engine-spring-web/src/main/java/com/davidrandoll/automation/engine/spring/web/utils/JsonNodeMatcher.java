package com.davidrandoll.automation.engine.spring.web.utils;

import com.davidrandoll.automation.engine.spring.web.exceptions.AutomationEngineInvalidRegexException;
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

    private static boolean matchesNode(JsonNode expected, JsonNode actual) {
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
            JsonNode actualValue = getPathWithWildcards(actual, expectedField.getKey());
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

    static JsonNode toJsonNode(Object obj, ObjectMapper mapper) {
        return obj instanceof JsonNode node ? node : mapper.convertValue(obj, JsonNode.class);
    }

    static boolean isNull(JsonNode node) {
        return node == null || node.isNull();
    }

    public static JsonNode getPathWithWildcards(JsonNode root, String path) {
        String[] parts = path.split("\\.", -1);
        return findRecursive(root, parts, 0);
    }

    private static JsonNode findRecursive(JsonNode current, String[] parts, int index) {
        if (current == null || index >= parts.length) return current;

        String key = parts[index].trim();

        if ("*".equals(key)) {
            for (Map.Entry<String, JsonNode> entry : iterable(current.fields())) {
                JsonNode result = findRecursive(entry.getValue(), parts, index + 1);
                if (result != null) return result;
            }
            return null;
        } else {
            JsonNode next = getFieldRegexAndCaseInsensitive(current, key);
            return findRecursive(next, parts, index + 1);
        }
    }

    private static JsonNode getFieldRegexAndCaseInsensitive(JsonNode node, String key) {
        // First, try exact (case-insensitive)
        for (Map.Entry<String, JsonNode> field : iterable(node.fields())) {
            if (field.getKey().equalsIgnoreCase(key))
                return field.getValue();
        }

        // Try regex match
        try {
            Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
            for (Map.Entry<String, JsonNode> field : iterable(node.fields())) {
                if (pattern.matcher(field.getKey()).matches()) return field.getValue();
            }
        } catch (PatternSyntaxException e) {
            throw new AutomationEngineInvalidRegexException(key, e);
        }

        return null;
    }


    static <T> Iterable<T> iterable(Iterator<T> iterator) {
        return () -> iterator;
    }
}