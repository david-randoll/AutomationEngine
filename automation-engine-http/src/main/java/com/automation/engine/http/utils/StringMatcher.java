package com.automation.engine.http.utils;

import com.automation.engine.http.modules.conditions.StringMatchContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

import static com.automation.engine.http.utils.JsonNodeMatcher.*;

@UtilityClass
public class StringMatcher {
    public static boolean matchesCondition(Object condition, Object actual, ObjectMapper mapper) {
        if (condition instanceof StringMatchContext ctx) {
            return ctx.matches(actual);
        }

        if (condition instanceof Map<?, ?> map) {
            if (actual instanceof Map<?, ?> actualMap) {
                return matchesMap(mapper, map, actualMap);
            }
            if (actual instanceof JsonNode actualNode) {
                return matchesJsonNode(mapper, map, actualNode);
            }
        }

        // Try JsonNode fallback
        JsonNode expectedNode = toJsonNode(condition, mapper);
        JsonNode actualNode = toJsonNode(actual, mapper);

        return matchesNode(expectedNode, actualNode, mapper);
    }

    private static boolean matchesNode(JsonNode expected, JsonNode actual, ObjectMapper mapper) {
        if (isLikelyMatchContext(mapper, expected)) {
            StringMatchContext ctx = mapper.convertValue(expected, StringMatchContext.class);
            if (isNull(actual))
                return matchesCondition(ctx, null, mapper);
            if (actual.isArray()) {
                var list = mapper.convertValue(actual, List.class);
                return matchesCondition(ctx, list, mapper);
            } else if (actual.isObject()) {
                var map = mapper.convertValue(actual, Map.class);
                return matchesCondition(ctx, map, mapper);
            }
            return matchesCondition(ctx, actual.asText(), mapper);
        }

        if (isNull(expected)) return true;
        if (isNull(actual)) return false;

        if (expected.isObject()) {
            return matchesJsonObject(expected, actual, mapper);
        }

        if (expected.isArray()) {
            return matchesJsonArray(expected, actual, mapper);
        }

        return false;
    }

    private static boolean matchesJsonObject(JsonNode expected, JsonNode actual, ObjectMapper mapper) {
        for (Map.Entry<String, JsonNode> expectedField : iterable(expected.fields())) {
            JsonNode actualValue = getFieldIgnoreCase(actual, expectedField.getKey());
            if (!matchesNode(expectedField.getValue(), actualValue, mapper)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesJsonArray(JsonNode expected, JsonNode actual, ObjectMapper mapper) {
        if (expected.isEmpty() && actual.isEmpty()) return true;
        if (!actual.isArray()) {
            for (JsonNode expectedElement : expected) {
                if (matchesNode(expectedElement, actual, mapper)) return true;
            }
            return false;
        }
        for (JsonNode expectedElement : expected) {
            for (JsonNode actualElement : actual) {
                if (matchesNode(expectedElement, actualElement, mapper)) return true;
            }
        }
        return false;
    }

    private static boolean matchesJsonNode(ObjectMapper mapper, Map<?, ?> map, JsonNode actualNode) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object expectedSub = entry.getValue();
            JsonNode actualSub = actualNode.get(String.valueOf(key));
            if (!matchesCondition(expectedSub, actualSub, mapper)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesMap(ObjectMapper mapper, Map<?, ?> map, Map<?, ?> actualMap) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object expectedSub = entry.getValue();
            Object actualSub = actualMap.get(key);
            if (!matchesCondition(expectedSub, actualSub, mapper)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLikelyMatchContext(ObjectMapper mapper, JsonNode node) {
        if (!node.isObject()) return false;
        var ctx = mapper.convertValue(node, StringMatchContext.class);
        return ctx.hasAnyOperations();
    }
}