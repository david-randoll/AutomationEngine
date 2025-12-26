package com.davidrandoll.automation.engine.templating;

import java.util.Map;

/**
 * Interface for templating engines.
 */
@FunctionalInterface
public interface ITemplateEngine {
    /**
     * Processes a template string with the provided variables.
     *
     * @param templateString The template string to process.
     * @param variables      A map of variables to be used in the template.
     * @return The rendered template as a string.
     */
    String process(String templateString, Map<String, Object> variables);
}