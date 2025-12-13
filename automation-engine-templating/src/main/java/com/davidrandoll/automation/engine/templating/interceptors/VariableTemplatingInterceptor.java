package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableChain;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Interceptor for processing variable data using templating.
 * <p>
 * This interceptor processes the variable context data by replacing any
 * placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class VariableTemplatingInterceptor implements IVariableInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts a variable to process and apply templated data.
     * <p>
     * Assumes that all strings in the action context are templated.
     * For example, the context map could contain a key-value pair like:
     * "message": "Hello {{name}}". The templateProcessor will replace
     * the placeholder {{name}} with the corresponding value from the event context.
     * If a string does not contain a template, it remains unchanged.
     *
     * @param eventContext    the event context providing data for template
     *                        replacement
     * @param variableContext the variable context containing data to process
     * @param chain           the variable to set after processing
     */

    @Override
    @SneakyThrows
    public void intercept(EventContext eventContext, VariableContext variableContext, IVariableChain chain) {
        log.debug("VariableTemplatingInterceptor: Processing variable data...");
        if (isFalse(chain.autoEvaluateExpression())) {
            chain.resolve(eventContext, variableContext);
            return;
        }

        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(variableContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            chain.resolve(eventContext, variableContext);
            return;
        }

        var mapCopy = processor.processIfNotAutomation(eventData, variableContext.getData());
        chain.resolve(eventContext, new VariableContext(mapCopy));
        log.debug("VariableTemplatingInterceptor done.");
    }

    public static class AutomationEngineProcessingException extends RuntimeException {
        public AutomationEngineProcessingException(Throwable cause) {
            super(cause);
        }
    }
}