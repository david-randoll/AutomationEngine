package com.davidrandoll.automation.engine.templating;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
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
    private final Map<String, ITemplateEngine> engines;
    private final String defaultEngine;
    private final ObjectMapper mapper;

    /**
     * Processes a template string with the provided variables.
     *
     * @param templateString The template string to process.
     * @param variables      A map of variables to be used in the template.
     * @return The rendered template as an Object (String for Pebble, Object for SpEL).
     * @throws IOException If there is an error during template processing.
     */
    public Object process(String templateString, Map<String, Object> variables) throws IOException {
        return process(templateString, variables, defaultEngine);
    }

    public Object process(String templateString, Map<String, Object> variables, String templatingType) {
        // Copy variables to ensure compatibility with the templating engine
        // Some engines like Pebble may have issues with certain data structures (like JsonNode)
        Map<String, Object> processedVariables = mapper.convertValue(variables, new TypeReference<>() {
        });

        // Try exact match first, then case-insensitive match
        ITemplateEngine engine = engines.get(templatingType);
        if (engine == null && templatingType != null) {
            // Try case-insensitive lookup
            engine = engines.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(templatingType))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        
        // Fallback to default engine
        if (engine == null) {
            engine = engines.get(defaultEngine);
        }

        if (engine == null) {
            throw new IllegalArgumentException("No templating engine found for type: " + templatingType + " and default engine: " + defaultEngine);
        }

        return engine.process(templateString, processedVariables);
    }
}