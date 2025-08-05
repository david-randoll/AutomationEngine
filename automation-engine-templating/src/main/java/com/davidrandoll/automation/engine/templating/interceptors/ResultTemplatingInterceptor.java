package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultChain;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * Interceptor for processing result data using templating.
 * <p>
 * This interceptor processes the result context data by replacing any placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@Component("resultTemplatingInterceptor")
@RequiredArgsConstructor
@Order(-1)
@ConditionalOnMissingBean(name = "resultTemplatingInterceptor", ignored = ResultTemplatingInterceptor.class)
public class ResultTemplatingInterceptor implements IResultInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    public Object intercept(EventContext eventContext, ResultContext resultContext, IResultChain chain) {
        log.debug("ConditionTemplatingInterceptor: Processing result data...");
        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(resultContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            return chain.getExecutionSummary(eventContext, resultContext);
        }

        JsonNode jsonNodeCopy = objectMapper.valueToTree(resultContext.getData()); // Create a copy of the data
        jsonNodeCopy = processor.processIfNotAutomation(eventData, jsonNodeCopy);

        var res = chain.getExecutionSummary(eventContext, new ResultContext(jsonNodeCopy));
        log.debug("ConditionTemplatingInterceptor: Condition data processed successfully.");
        return res;
    }

    public static class AutomationEngineProcessingException extends RuntimeException {
        public AutomationEngineProcessingException(Throwable cause) {
            super(cause);
        }
    }
}