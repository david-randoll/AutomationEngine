package com.davidrandoll.automation.engine.templating.engines;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Implementation of {@link ITemplateEngine} using the Pebble templating engine.
 */
@RequiredArgsConstructor
public class PebbleTemplateEngine implements ITemplateEngine {
    private final PebbleEngine pebbleEngine;

    @Override
    public String getType() {
        return "pebble";
    }

    @Override
    public String process(String templateString, Map<String, Object> variables) {
        try {
            PebbleTemplate template = pebbleEngine.getLiteralTemplate(templateString);
            try (StringWriter writer = new StringWriter()) {
                template.evaluate(writer, variables);
                return writer.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing Pebble template", e);
        }
    }
}
