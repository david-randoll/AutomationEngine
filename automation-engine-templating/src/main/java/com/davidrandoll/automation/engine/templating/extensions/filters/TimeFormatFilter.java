package com.davidrandoll.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TimeFormatFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return List.of("pattern");
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input instanceof LocalTime localTime) {
            return formatLocalTimeToUserFriendlyName(args, localTime);
        } else if (input instanceof String string) {
            var localTime = LocalTime.parse(string);
            return formatLocalTimeToUserFriendlyName(args, localTime);
        }
        return input;
    }

    private static String formatLocalTimeToUserFriendlyName(Map<String, Object> args, LocalTime localTime) {
        String pattern = args.getOrDefault("pattern", "hh:mm a").toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localTime.format(formatter);
    }
}
