package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionChain;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.Map;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Interceptor for processing action data using templating.
 * <p>
 * This interceptor processes the action context data by replacing any
 * placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class ActionTemplatingInterceptor implements IActionInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts an action to process and apply templated data.
     * <p>
     * Assumes that all strings in the action context are templated.
     * For example, the action context map could contain a key-value pair like:
     * "message": "Hello {{name}}". The templateProcessor will replace
     * the placeholder {{name}} with the corresponding value from the event context.
     * If a string does not contain a template, it remains unchanged.
     *
     * @param eventContext  the event context providing data for template
     *                      replacement
     * @param actionContext the action context containing data to process
     * @param chain         the action to be executed after processing
     */
    @Override
    public void intercept(EventContext eventContext, ActionContext actionContext, IActionChain chain) {
        log.debug("ActionTemplatingInterceptor: Processing action data...");
        if (isFalse(chain.autoEvaluateExpression())) {
            chain.execute(eventContext, actionContext);
            return;
        }

        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(actionContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            chain.execute(eventContext, actionContext);
            return;
        }

        String templatingType = getTemplatingType(actionContext.getOptions());
        var mapCopy = processor.processIfNotAutomation(eventData, actionContext.getData(), templatingType);
        chain.execute(eventContext, actionContext.changeData(mapCopy));
        log.debug("ActionTemplatingInterceptor done.");
    }

    private String getTemplatingType(Map<String, Object> options) {
        if (ObjectUtils.isEmpty(options)) {
            return "pebble";
        }
        Object type = options.getOrDefault("templatingType", options.get("templateType"));
        return type instanceof String s ? s : "pebble";
    }
}