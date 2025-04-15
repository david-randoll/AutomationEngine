package com.automation.engine.http.jackson.flexible_httpstatus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlexibleHttpStatusListDeserializer extends JsonDeserializer<List<HttpStatus>> {

    @Override
    public List<HttpStatus> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        if (node.isInt()) {
            HttpStatus status = HttpStatus.valueOf(node.asInt());
            return List.of(status);
        }

        if (node.isTextual()) {
            HttpStatus status = HttpStatus.valueOf(node.asText());
            return List.of(status);
        }

        if (node.isArray()) {
            List<HttpStatus> statuses = new ArrayList<>();
            for (JsonNode statusNode : node) {
                HttpStatus status = HttpStatus.valueOf(statusNode.asText());
                statuses.add(status);
            }
            return statuses;
        }

        throw new IllegalArgumentException("Unsupported value type for methods: " + node.getNodeType());
    }
}