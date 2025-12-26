package com.davidrandoll.automation.engine.templating;


import com.fasterxml.jackson.core.type.TypeReference;
import com.davidrandoll.automation.engine.templating.engines.ITemplateEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
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
    private final List<ITemplateEngine> engines;
    private final String defaultEngine;
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
        return process(templateString, variables, defaultEngine);
    }

    public String process(String templateString, Map<String, Object> variables, String templatingType) throws IOException {
        Map<String, Object> processedVariables = mapper.convertValue(variables, new TypeReference<>() {
        });

        ITemplateEngine engine = engines.stream()
                .filter(e -> e.getType().equalsIgnoreCase(templatingType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No template engine found for type: " + templatingType));

        return engine.process(templateString, processedVariables);
    }
}