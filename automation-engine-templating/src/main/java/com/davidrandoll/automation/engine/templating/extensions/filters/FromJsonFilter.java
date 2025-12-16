package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Parses a JSON string into an object/map.
 * <p>
 * Usage: {{ jsonString | fromJson }}
 * Access nested properties: {{ jsonString | fromJson.user.name }}
 */
@RequiredArgsConstructor
public class FromJsonFilter implements Filter {
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getArgumentNames() {
        return List.of();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return null;
        }

        if (!(input instanceof String)) {
            throw new PebbleException(null, "fromJson filter expects a string input", lineNumber, self != null ? self.getName() : null);
        }

        String jsonString = (String) input;
        try {
            // Parse as generic object (will be Map for objects, List for arrays, or primitives)
            return objectMapper.readValue(jsonString, Object.class);
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to parse JSON string", lineNumber, self != null ? self.getName() : null);
        }
    }
}

