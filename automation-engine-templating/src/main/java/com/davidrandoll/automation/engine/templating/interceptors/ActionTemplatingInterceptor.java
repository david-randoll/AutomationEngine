package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for processing action data using templating.
 * <p>
 * This interceptor processes the action context data by replacing any placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@Component("actionTemplatingInterceptor")
@RequiredArgsConstructor
@Order(-1)
@ConditionalOnMissingBean(name = "actionTemplatingInterceptor", ignored = ActionTemplatingInterceptor.class)
public class ActionTemplatingInterceptor implements IActionInterceptor {
    private final TemplateProcessor templateProcessor;
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
     * @param eventContext  the event context providing data for template replacement
     * @param actionContext the action context containing data to process
     * @param action        the action to be executed after processing
     */

    @Override
    public void intercept(EventContext eventContext, ActionContext actionContext, IAction action) {
        log.debug("ActionTemplatingInterceptor: Processing action data...");
        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(actionContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            action.execute(eventContext, actionContext);
        }

        var mapCopy = new HashMap<>(actionContext.getData());
        for (Map.Entry<String, Object> entry : mapCopy.entrySet()) {
            if (entry.getValue() instanceof String valueStr) {
                try {
                    String processedValue = templateProcessor.process(valueStr, eventData);
                    entry.setValue(processedValue);
                } catch (IOException e) {
                    log.error("Error processing template for key: {}. Error: {}", entry.getKey(), e.getMessage());
                    throw new AutomationEngineProcessingException(e);
                }
            }
        }
        action.execute(eventContext, new ActionContext(mapCopy));
        log.debug("ActionTemplatingInterceptor done.");
    }

    public static class AutomationEngineProcessingException extends RuntimeException {
        public AutomationEngineProcessingException(Throwable cause) {
            super(cause);
        }
    }
}