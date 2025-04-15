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

        if (node.isInt() || node.isTextual()) {
            return List.of(parseStatus(node));
        }

        if (node.isArray()) {
            List<HttpStatus> statuses = new ArrayList<>();
            for (JsonNode statusNode : node) {
                statuses.add(parseStatus(statusNode));
            }
            return statuses;
        }

        throw new IllegalArgumentException("Unsupported value type for HTTP status: " + node.getNodeType());
    }

    private HttpStatus parseStatus(JsonNode node) {
        if (node.isInt()) {
            return HttpStatus.valueOf(node.asInt());
        } else if (node.isTextual()) {
            String text = node.asText().trim();
            try {
                return HttpStatus.valueOf(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                // Normalize text to upper snake case (e.g. "ok" -> "OK", "not_found" -> "NOT_FOUND")
                String normalized = text.toUpperCase().replace('-', '_').replace(' ', '_');
                return HttpStatus.valueOf(normalized);
            }
        } else {
            throw new IllegalArgumentException("Invalid HTTP status value: " + node);
        }
    }
}