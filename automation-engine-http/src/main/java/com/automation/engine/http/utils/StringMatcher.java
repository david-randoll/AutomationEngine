package com.automation.engine.http.utils;

import com.automation.engine.http.modules.conditions.StringMatchContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.stream.Stream;

import static com.automation.engine.http.utils.JsonNodeMatcher.matchesNode;
import static com.automation.engine.http.utils.JsonNodeMatcher.toJsonNode;

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

        if (isLikelyMatchContext(expectedNode)) {
            StringMatchContext ctx = mapper.convertValue(expectedNode, StringMatchContext.class);
            return ctx.matches(actualNode.asText());
        }

        return matchesNode(expectedNode, actualNode);
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

    private static boolean isLikelyMatchContext(JsonNode node) {
        if (!node.isObject()) return false;
        return Stream.of("equals", "notEquals", "in", "regex", "like", "match")
                .anyMatch(node::has);
    }
}