package com.davidrandoll.automation.engine.spring.notification.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for email address lists.
 * <p>
 * Supports multiple input formats:
 * <ul>
 *     <li>Array: ["email1@test.com", "email2@test.com"]</li>
 *     <li>Comma-separated string: "email1@test.com,email2@test.com"</li>
 *     <li>Semicolon-separated string: "email1@test.com;email2@test.com"</li>
 *     <li>Single string: "email@test.com"</li>
 * </ul>
 * </p>
 */
public class EmailListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        List<String> emails = new ArrayList<>();

        if (node.isArray()) {
            // Handle array format: ["email1", "email2"]
            for (JsonNode element : node) {
                if (element.isTextual()) {
                    String email = element.asText().trim();
                    if (!email.isEmpty()) {
                        emails.add(email);
                    }
                }
            }
        } else if (node.isTextual()) {
            // Handle string format (comma/semicolon separated or single)
            String text = node.asText().trim();
            if (!text.isEmpty()) {
                // Split by comma or semicolon
                String[] parts = text.split("[,;]");
                for (String part : parts) {
                    String email = part.trim();
                    if (!email.isEmpty()) {
                        emails.add(email);
                    }
                }
            }
        }

        return emails;
    }
}
