package com.davidrandoll.automation.engine.spring.web.jackson.flexible_httpstatus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        }

        if (node.isTextual()) {
            String text = node.asText().trim();
            String normalized = text.toUpperCase().replace('-', '_').replace(' ', '_');
            for (HttpStatus status : HttpStatus.values()) {
                if (status.name().equalsIgnoreCase(normalized) || String.valueOf(status.value()).equalsIgnoreCase(normalized)) {
                    return status;
                }
            }
        }

        throw new IllegalArgumentException("Invalid HTTP status value: " + node);
    }
}