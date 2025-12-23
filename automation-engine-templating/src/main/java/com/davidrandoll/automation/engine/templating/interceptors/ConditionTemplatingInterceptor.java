package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Interceptor for processing condition data using templating.
 * <p>
 * This interceptor processes the condition context data by replacing any placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class ConditionTemplatingInterceptor implements IConditionInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public boolean intercept(EventContext eventContext, ConditionContext conditionContext, IConditionChain chain) {
        log.debug("ConditionTemplatingInterceptor: Processing condition data...");
        if (isFalse(chain.autoEvaluateExpression())) {
            return chain.isSatisfied(eventContext, conditionContext);
        }

        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(conditionContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            return chain.isSatisfied(eventContext, conditionContext);
        }

        var mapCopy = processor.processIfNotAutomation(eventData, conditionContext.getData());
        var result = chain.isSatisfied(eventContext, conditionContext.changeData(mapCopy));
        log.debug("ConditionTemplatingInterceptor: Condition data processed successfully.");
        return result;
    }
}