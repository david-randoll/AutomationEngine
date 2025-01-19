package com.automation.engine.templating;


import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class TemplateProcessor {

    private final PebbleEngine pebbleEngine;

    public TemplateProcessor() {
        this.pebbleEngine = new PebbleEngine.Builder()
                .extension(new CustomExtension())
                .build();
    }

    /**
     * Processes a template string with the provided variables.
     *
     * @param templateString The template string to process.
     * @param variables      A map of variables to be used in the template.
     * @return The rendered template as a string.
     * @throws Exception If there is an error during template processing.
     */
    public String process(String templateString, Map<String, Object> variables) throws Exception {
        PebbleTemplate template = pebbleEngine.getLiteralTemplate(templateString);

        try (StringWriter writer = new StringWriter()) {
            template.evaluate(writer, variables);
            return writer.toString();
        }
    }

    public static class NumberFormatFilter implements Filter {

        @Override
        public List<String> getArgumentNames() {
            return List.of("decimalPlaces");
        }

        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
            if (input instanceof Number) {
                // Get the decimalPlaces argument, default to 2 if not provided
                int decimalPlaces = 2; // default value
                if (args.containsKey("decimalPlaces")) {
                    Object decimalArg = args.get("decimalPlaces");
                    if (decimalArg instanceof Number) {
                        decimalPlaces = ((Number) decimalArg).intValue();
                    }
                }

                // Build the pattern dynamically based on decimal places
                StringBuilder pattern = new StringBuilder("#");
                if (decimalPlaces > 0) {
                    pattern.append(".");
                    pattern.append("0".repeat(decimalPlaces));
                }

                // Format the number
                DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
                return decimalFormat.format(input);
            }
            return input; // return the input if it's not a number
        }
    }

    public static class CustomExtension extends AbstractExtension {
        @Override
        public Map<String, Filter> getFilters() {
            return Map.of("number_format", new NumberFormatFilter());
        }
    }

}

