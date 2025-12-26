package com.davidrandoll.automation.engine.templating.pebbles.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.List;
import java.util.Map;

public class IntegerFilter implements Filter {
    @Override
    public List<String> getArgumentNames() {
        return List.of();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return 0;
        }
        if (input instanceof Number num) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(input.toString().trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Cannot convert to int: " + input);
        }
    }
}