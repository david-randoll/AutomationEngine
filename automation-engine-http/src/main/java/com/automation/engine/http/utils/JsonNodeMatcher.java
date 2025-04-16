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
            if (!actual.isArray()) {
                // actual is a single value, check if any expected element matches it
                for (JsonNode expectedElement : expected) {
                    if (checkJsonNode(expectedElement, actual)) {
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

        String expectedText = expected.asText();
        String actualText = actual.asText("");
        var pattern = Pattern.compile(expectedText);
        return pattern.matcher(actualText).matches();
    }
}