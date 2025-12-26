package com.davidrandoll.automation.engine.templating.engines;

import java.util.Map;

/**
 * Interface for templating engines.
 */
public interface ITemplateEngine {
    /**
     * Returns the type of the templating engine.
     *
     * @return The type of the templating engine.
     */
    String getType();

    /**
     * Processes a template string with the provided variables.
     *
     * @param templateString The template string to process.
     * @param variables      A map of variables to be used in the template.
     * @return The rendered template as a string.
     */
    String process(String templateString, Map<String, Object> variables);
}
