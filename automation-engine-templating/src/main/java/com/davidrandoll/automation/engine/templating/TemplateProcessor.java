package com.davidrandoll.automation.engine.templating;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * A service for processing templates using the Pebble templating engine.
 * <p>
 * This class provides a method to process a template string with the provided variables.
 * It uses the PebbleEngine to evaluate the template and return the rendered result.
 * </p>
 */
@RequiredArgsConstructor
public class TemplateProcessor {
    private final PebbleEngine pebbleEngine;
    private final ObjectMapper mapper;

    /**
     * Processes a template string with the provided variables.
     *
     * @param templateString The template string to process.
     * @param variables      A map of variables to be used in the template.
     * @return The rendered template as a string.
     * @throws IOException If there is an error during template processing.
     */
    public String process(String templateString, Map<String, Object> variables) throws IOException {
        // need to ensure that variables don't have any JsonNode values. because pebble can't handle them directly
        Map<String, Object> processedVariables = mapper.convertValue(variables, new TypeReference<>() {
        });

        PebbleTemplate template = pebbleEngine.getLiteralTemplate(templateString);

        try (StringWriter writer = new StringWriter()) {
            template.evaluate(writer, processedVariables);
            return writer.toString();
        }
    }
}