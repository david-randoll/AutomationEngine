package com.davidrandoll.automation.engine.templating.pebbles.extensions.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Converts an object to its JSON string representation.
 * <p>
 * Usage: {{ event.user | json }}
 * Usage with pretty print: {{ event.user | json(pretty=true) }}
 */
@RequiredArgsConstructor
public class JsonFilter implements Filter {
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getArgumentNames() {
        return List.of("pretty");
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return "null";
        }

        try {
            boolean pretty = false;
            if (args.containsKey("pretty")) {
                Object prettyArg = args.get("pretty");
                if (prettyArg instanceof Boolean bool) {
                    pretty = bool;
                }
            }

            if (pretty) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
            } else {
                return objectMapper.writeValueAsString(input);
            }
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to convert object to JSON", lineNumber, self.getName());
        }
    }
}


