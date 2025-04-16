package com.automation.engine.http.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

@UtilityClass
public class JsonNodeMatcher {

    public static boolean checkObject(Object expected, Object actual, ObjectMapper mapper) {
        JsonNode expectedNode = mapper.convertValue(expected, JsonNode.class);
        JsonNode actualNode = mapper.convertValue(actual, JsonNode.class);
        return checkJsonNode(expectedNode, actualNode);
    }

    public static boolean checkJsonNode(JsonNode expected, JsonNode actual) {
        if (expected == null || expected.isNull()) {
            return true; // null expected = wildcard
        }
        if (actual == null || actual.isNull()) {
            return false;
        }

        if (expected.isObject()) {
            // Must match all fields in the expected object
            for (Iterator<Map.Entry<String, JsonNode>> it = expected.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> expectedField = it.next();
                String expectedKey = expectedField.getKey();
                JsonNode expectedValue = expectedField.getValue();
                JsonNode actualValue = getJsonNodeCaseInsensitive(actual, expectedKey);
                if (!checkJsonNode(expectedValue, actualValue)) {
                    return false;
                }
            }
            return true;
        }

        if (expected.isArray()) {
            // All expected elements must be present in actual array
            if (!actual.isArray()) {
                // actual is a single value, check if any expected element matches it
                for (JsonNode expectedElement : expected) {
                    if (!checkJsonNode(expectedElement, actual)) {
                        return false;
                    }
                }
                return true;
            }
            for (JsonNode expectedElement : expected) {
                boolean matched = false;
                for (JsonNode actualElement : actual) {
                    if (checkJsonNode(expectedElement, actualElement)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) return false;
            }
            return true;
        }

        String expectedText = expected.asText().trim();
        String actualText = actual.asText().trim();
        var pattern = Pattern.compile(expectedText);
        return pattern.matcher(actualText).matches();
    }

    public JsonNode getJsonNodeCaseInsensitive(JsonNode actual, String key) {
        for (Iterator<Map.Entry<String, JsonNode>> it = actual.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> actualField = it.next();
            if (actualField.getKey().equalsIgnoreCase(key)) {
                return actualField.getValue();
            }
        }
        return null;
    }
}