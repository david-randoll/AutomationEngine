package com.automation.engine.http.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.Map;

@UtilityClass
public class JsonNodeMatcher {

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
                Map.Entry<String, JsonNode> field = it.next();
                String key = field.getKey();
                JsonNode expectedValue = field.getValue();
                JsonNode actualValue = actual.get(key);
                if (!checkJsonNode(expectedValue, actualValue)) {
                    return false;
                }
            }
            return true;
        }

        if (expected.isArray()) {
            // All expected elements must be present in actual array
            if (!actual.isArray()) return false;
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

        // Primitive types: support regex if starts with "~"
        if (expected.isTextual()) {
            String expectedText = expected.asText();
            String actualText = actual.asText("");
            if (expectedText.startsWith("~")) {
                String regex = expectedText.substring(1);
                return actualText.matches(regex);
            } else {
                return expectedText.equals(actualText);
            }
        }

        return expected.equals(actual);
    }
}