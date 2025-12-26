package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerChain;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Interceptor for processing trigger data using templating.
 * <p>
 * This interceptor processes the trigger context data by replacing any
 * placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class TriggerTemplatingInterceptor implements ITriggerInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public boolean intercept(EventContext eventContext, TriggerContext triggerContext, ITriggerChain chain) {
        log.debug("TriggerTemplatingInterceptor: Processing trigger data...");
        if (isFalse(chain.autoEvaluateExpression())) {
            return chain.isTriggered(eventContext, triggerContext);
        }

        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(triggerContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            return chain.isTriggered(eventContext, triggerContext);
        }

        String templatingType = processor.getTemplatingType(eventContext, triggerContext.getOptions());
        var mapCopy = processor.processIfNotAutomation(eventData, triggerContext.getData(), templatingType);
        var result = chain.isTriggered(eventContext, triggerContext.changeData(mapCopy));
        log.debug("TriggerTemplatingInterceptor: Trigger data processed successfully.");
        return result;
    }
}
