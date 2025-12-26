package com.davidrandoll.automation.engine.templating.pebbles.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Decodes a Base64 string.
 * <p>
 * Usage: {{ encodedString | base64decode }}
 */
public class Base64DecodeFilter implements Filter {

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
            throw new PebbleException(null, "base64decode filter expects a string input", lineNumber, self != null ? self.getName() : null);
        }

        try {
            String inputStr = (String) input;
            byte[] decodedBytes = Base64.getDecoder().decode(inputStr);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to decode Base64 string", lineNumber, self != null ? self.getName() : null);
        }
    }
}

