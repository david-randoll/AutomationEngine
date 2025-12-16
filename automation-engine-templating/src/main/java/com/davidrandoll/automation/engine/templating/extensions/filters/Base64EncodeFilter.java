package com.davidrandoll.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Encodes a string to Base64.
 * <p>
 * Usage: {{ 'username:password' | base64encode }}
 * Usage in HTTP header: Authorization: Basic {{ credentials | base64encode }}
 */
public class Base64EncodeFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return List.of();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return null;
        }

        try {
            String inputStr = input.toString();
            return Base64.getEncoder().encodeToString(inputStr.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to encode to Base64", lineNumber, self.getName());
        }
    }
}

