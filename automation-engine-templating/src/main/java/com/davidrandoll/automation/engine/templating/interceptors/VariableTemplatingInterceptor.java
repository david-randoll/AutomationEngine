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

        String templatingType = processor.getTemplatingType(variableContext.getOptions());
        var mapCopy = processor.processIfNotAutomation(eventData, variableContext.getData(), templatingType);
        chain.resolve(eventContext, variableContext.changeData(mapCopy));
        log.debug("VariableTemplatingInterceptor: Variable data processed successfully.");
    }
}
