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
import org.springframework.util.ObjectUtils;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Interceptor for processing result data using templating.
 * <p>
 * This interceptor processes the result context data by replacing any
 * placeholders
 * in the strings with corresponding values from the event context.
 * It uses a {@link TemplateProcessor} to perform the templating.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class ResultTemplatingInterceptor implements IResultInterceptor {
    private final JsonNodeVariableProcessor processor;
    private final ObjectMapper objectMapper;

    @Override
    public Object intercept(EventContext eventContext, ResultContext resultContext, IResultChain chain) {
        log.debug("ResultTemplatingInterceptor: Processing result data...");
        if (isFalse(chain.autoEvaluateExpression())) {
            return chain.getExecutionSummary(eventContext, resultContext);
        }

        var eventData = eventContext.getEventData(objectMapper);
        if (ObjectUtils.isEmpty(resultContext.getData()) || ObjectUtils.isEmpty(eventData)) {
            return chain.getExecutionSummary(eventContext, resultContext);
        }

        String templatingType = processor.getTemplatingType(eventContext, resultContext.getOptions());
        JsonNode jsonNodeCopy = objectMapper.valueToTree(resultContext.getData()); // Create a copy of the data
        jsonNodeCopy = processor.processIfNotAutomation(eventData, jsonNodeCopy, templatingType);

        var res = chain.getExecutionSummary(eventContext, resultContext.changeData(jsonNodeCopy));
        log.debug("ResultTemplatingInterceptor: Result data processed successfully.");
        return res;
    }
}
