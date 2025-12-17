package com.davidrandoll.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Returns the first non-null value from the input or arguments.
 * <p>
 * Usage: {{ event.assignee | coalesce(event.creator, 'unassigned') }}
 * Usage: {{ nullableValue | coalesce('default') }}
 */
public class CoalesceFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return null; // Accept variable number of arguments
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        // First check if input is non-null
        if (input != null) {
            return input;
        }

        // Then check positional arguments
        if (args != null && !args.isEmpty()) {
            // Pebble passes positional args with numeric string keys: "0", "1", "2", etc.
            List<Object> positionalArgs = new ArrayList<>();
            int i = 0;
            while (args.containsKey(String.valueOf(i))) {
                positionalArgs.add(args.get(String.valueOf(i)));
                i++;
            }

            for (Object arg : positionalArgs) {
                if (arg != null) {
                    return arg;
                }
            }
        }

        return null;
    }
}

