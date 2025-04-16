package com.automation.engine.http.jackson.flexible_map_object;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;

public class FlexibleMapObjectDeserializer extends JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        Map<String, Object> result = new LinkedHashMap<>();

        if (!node.isObject()) {
            throw new IllegalArgumentException("Expected JSON object for Map<String, Object> deserialization, but got: " + node.getNodeType());
        }

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value.isArray()) {
                List<Object> values = new ArrayList<>();
                for (JsonNode item : value) {
                    values.add(getValue(item));
                }
                result.put(key, values);
            } else {
                result.put(key, getValue(value));
            }
        }

        return result;
    }

    private Object getValue(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isObject()) return node;
        if (node.isNull()) return null;
        return node.toString(); // fallback
    }
}
