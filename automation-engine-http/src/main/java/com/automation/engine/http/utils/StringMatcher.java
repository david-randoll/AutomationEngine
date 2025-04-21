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
        if (condition == null) return true;
        if (actual == null) return false;

        if (condition instanceof StringMatchContext ctx) {
            return ctx.matches(String.valueOf(actual));
        }

        if (condition instanceof Map<?, ?> map) {
            if (actual instanceof Map<?, ?> actualMap) {
                for (Object key : map.keySet()) {
                    Object expectedSub = map.get(key);
                    Object actualSub = actualMap.get(key);
                    if (!matchesCondition(expectedSub, actualSub, mapper)) {
                        return false;
                    }
                }
                return true;
            }
            if (actual instanceof JsonNode actualNode) {
                for (Object key : map.keySet()) {
                    Object expectedSub = map.get(key);
                    JsonNode actualSub = actualNode.get(String.valueOf(key));
                    if (!matchesCondition(expectedSub, actualSub, mapper)) {
                        return false;
                    }
                }
                return true;
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

    private static boolean isLikelyMatchContext(JsonNode node) {
        if (!node.isObject()) return false;
        return Stream.of("equals", "notEquals", "in", "regex", "like", "match")
                .anyMatch(node::has);
    }
}