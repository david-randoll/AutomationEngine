package com.automation.engine.http.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class JsonNodeMatcher {
    public static boolean checkJsonNode(JsonNode queryParams, JsonNode eventQueryParams) {
        if (queryParams == null || queryParams.isNull() || eventQueryParams == null || eventQueryParams.isNull()) {
            return true;
        }

        // Build a lowercased map of event query params for case-insensitive key matching
        Map<String, List<String>> normalizedEventQueryParams = new HashMap<>();
        eventQueryParams.fieldNames().forEachRemaining(field -> {
            JsonNode valueNode = eventQueryParams.get(field);
            if (valueNode.isArray()) {
                List<String> values = new ArrayList<>();
                for (JsonNode val : valueNode) {
                    if (val != null && !val.isNull()) {
                        values.add(val.asText().trim());
                    }
                }
                normalizedEventQueryParams.put(field.trim().toLowerCase(), values);
            }
        });

        Iterator<Map.Entry<String, JsonNode>> fields = queryParams.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String expectedKey = field.getKey().trim().toLowerCase();

            List<String> expectedValues = new ArrayList<>();
            JsonNode valueNode = field.getValue();
            if (valueNode.isArray()) {
                for (JsonNode val : valueNode) {
                    if (val != null && !val.isNull()) {
                        expectedValues.add(val.asText().trim());
                    }
                }
            }

            List<String> actualValues = normalizedEventQueryParams.getOrDefault(expectedKey, List.of());
            if (actualValues.isEmpty()) return true;

            boolean noneMatched = expectedValues.stream().noneMatch(expectedPattern ->
                    actualValues.stream().anyMatch(actualValue -> actualValue.matches("(?i)" + expectedPattern))
            );

            if (noneMatched) return true;
        }

        return false;
    }
}
