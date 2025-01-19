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
}

