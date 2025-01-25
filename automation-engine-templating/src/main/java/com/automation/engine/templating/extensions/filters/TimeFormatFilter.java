package com.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component("time_format")
public class TimeFormatFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return List.of("pattern");
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input instanceof LocalTime localTime) {
            String pattern = args.getOrDefault("pattern", "hh:mm a").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return localTime.format(formatter);
        }
        return input;
    }
}
