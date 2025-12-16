package com.davidrandoll.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * URL-decodes a string.
 * <p>
 * Usage: {{ encodedParam | urlDecode }}
 */
public class UrlDecodeFilter implements Filter {

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
            throw new PebbleException(null, "urlDecode filter expects a string input", lineNumber, self != null ? self.getName() : null);
        }

        try {
            return URLDecoder.decode((String) input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to URL-decode string", lineNumber, self != null ? self.getName() : null);
        }
    }
}

