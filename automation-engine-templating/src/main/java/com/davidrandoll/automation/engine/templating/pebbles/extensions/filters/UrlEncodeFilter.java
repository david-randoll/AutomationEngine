package com.davidrandoll.automation.engine.templating.pebbles.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * URL-encodes a string for use in query parameters.
 * <p>
 * Usage: {{ searchTerm | urlEncode }}
 * Usage: /api/search?q={{ query | urlEncode }}
 */
public class UrlEncodeFilter implements Filter {

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
            return URLEncoder.encode(input.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to URL-encode string", lineNumber, self.getName());
        }
    }
}

