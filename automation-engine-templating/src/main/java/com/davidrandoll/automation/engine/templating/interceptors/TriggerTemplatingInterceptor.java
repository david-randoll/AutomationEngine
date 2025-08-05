package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.creator.triggers.EvaluatableTrigger;
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
 * Interceptor for processing trigger data using templating.
 * <p>
 * This interceptor processes the trigger context data by replacing any placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@Component("triggerTemplatingInterceptor")
@RequiredArgsConstructor
@Order(-1)
@ConditionalOnMissingBean(name = "triggerTemplatingInterceptor", ignored = TriggerTemplatingInterceptor.class)
public class TriggerTemplatingInterceptor implements ITriggerInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void intercept(EventContext eventContext, TriggerContext triggerContext, ITrigger trigger) {
        log.debug("TriggerTemplatingInterceptor: Processing trigger data...");
        if (trigger instanceof EvaluatableTrigger et && !et.getRawTrigger().autoEvaluateExpression()) {
            trigger.isTriggered(eventContext, triggerContext);
            return;
        }

        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(triggerContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            trigger.isTriggered(eventContext, triggerContext);
        }

        var mapCopy = processor.processIfNotAutomation(eventData, triggerContext.getData());
        trigger.isTriggered(eventContext, new TriggerContext(mapCopy));
        log.debug("TriggerTemplatingInterceptor: Trigger data processed successfully.");
    }

    public static class AutomationEngineProcessingException extends RuntimeException {
        public AutomationEngineProcessingException(Throwable cause) {
            super(cause);
        }
    }
}