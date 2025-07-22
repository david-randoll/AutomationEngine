package com.davidrandoll.automation.engine.http.jackson.flexible_map_object;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlexibleMapObjectDeserializer extends JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        if (!node.isObject()) {
            throw new IllegalArgumentException("Expected JSON object for Map<String, Object> deserialization, but got: " + node.getNodeType());
        }

        var objectMapper = (ObjectMapper) p.getCodec();
        return objectMapper.treeToValue(node, new TypeReference<HashMap<String, Object>>() {
        });
    }
}
