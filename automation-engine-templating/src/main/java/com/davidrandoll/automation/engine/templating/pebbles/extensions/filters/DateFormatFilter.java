package com.davidrandoll.automation.engine.templating.pebbles.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Formats date/time objects to a string using a pattern.
 * <p>
 * Supports: LocalDate, LocalDateTime, Instant, Date
 * <p>
 * Usage: {{ event.createdAt | dateFormat('yyyy-MM-dd HH:mm:ss') }}
 * Usage with default: {{ event.createdAt | dateFormat }}
 */
public class DateFormatFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return List.of("pattern");
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return null;
        }

        String pattern = args.getOrDefault("pattern", "yyyy-MM-dd HH:mm:ss").toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        try {
            if (input instanceof LocalDateTime localDateTime) {
                return localDateTime.format(formatter);
            } else if (input instanceof LocalDate localDate) {
                return localDate.format(formatter);
            } else if (input instanceof Instant instant) {
                return instant.atZone(ZoneId.systemDefault()).format(formatter);
            } else if (input instanceof Date date) {
                return date.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
            } else if (input instanceof String) {
                // Try to parse and format
                try {
                    LocalDateTime ldt = LocalDateTime.parse((String) input);
                    return ldt.format(formatter);
                } catch (Exception e) {
                    // Not a parseable date, return as-is
                    return input;
                }
            }
            return input;
        } catch (Exception e) {
            throw new PebbleException(e, "Failed to format date with pattern: " + pattern, lineNumber, self.getName());
        }
    }
}

