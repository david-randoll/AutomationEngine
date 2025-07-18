package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * Interceptor for processing condition data using templating.
 * <p>
 * This interceptor processes the condition context data by replacing any placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@Component("conditionTemplatingInterceptor")
@RequiredArgsConstructor
@Order(-1)
@ConditionalOnMissingBean(name = "conditionTemplatingInterceptor", ignored = ConditionTemplatingInterceptor.class)
public class ConditionTemplatingInterceptor implements IConditionInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void intercept(EventContext eventContext, ConditionContext conditionContext, ICondition condition) {
        log.debug("ConditionTemplatingInterceptor: Processing condition data...");
        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(conditionContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            condition.isSatisfied(eventContext, conditionContext);
        }

        var mapCopy = processor.processIfNotAutomation(eventData, conditionContext.getData());
        condition.isSatisfied(eventContext, new ConditionContext(mapCopy));
        log.debug("ConditionTemplatingInterceptor: Condition data processed successfully.");
    }

    public static class AutomationEngineProcessingException extends RuntimeException {
        public AutomationEngineProcessingException(Throwable cause) {
            super(cause);
        }
    }
}