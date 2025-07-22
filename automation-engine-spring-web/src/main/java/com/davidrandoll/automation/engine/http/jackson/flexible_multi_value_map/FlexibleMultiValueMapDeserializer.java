package com.davidrandoll.automation.engine.http.jackson.flexible_multi_value_map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

public class FlexibleMultiValueMapDeserializer extends JsonDeserializer<MultiValueMap<String, String>> {

    @Override
    public MultiValueMap<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        HttpHeaders headers = new HttpHeaders(); // works as MultiValueMap<String, String>

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();

                if (valueNode.isArray()) {
                    for (JsonNode val : valueNode) {
                        headers.add(key, val.asText());
                    }
                } else {
                    headers.add(key, valueNode.asText());
                }
            });
            return headers;
        }

        throw new IllegalArgumentException("Unsupported value type for MultiValueMap: " + node.getNodeType());
    }
}
