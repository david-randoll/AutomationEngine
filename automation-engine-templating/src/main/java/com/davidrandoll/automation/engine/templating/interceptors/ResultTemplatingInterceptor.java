package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper mapper;

    @Override
    public Object intercept(EventContext eventContext, ResultContext resultContext, IResult result) {
        log.debug("ConditionTemplatingInterceptor: Processing result data...");
        if (ObjectUtils.isEmpty(resultContext.getData()) || ObjectUtils.isEmpty(eventContext.getEventData())) {
            return result.getExecutionSummary(eventContext, resultContext);
        }

        JsonNode jsonNodeCopy = mapper.valueToTree(resultContext.getData()); // Create a copy of the data
        jsonNodeCopy = processIfNotAutomation(eventContext, jsonNodeCopy);

        var res = result.getExecutionSummary(eventContext, new ResultContext(jsonNodeCopy));
        log.debug("ConditionTemplatingInterceptor: Condition data processed successfully.");
        return res;
    }

    private static final Set<String> AUTOMATION_FIELDS = Set.of("action", "variable", "condition", "trigger", "result");

    private JsonNode processIfNotAutomation(EventContext eventContext, JsonNode node) {
        if (node == null || node.isNull()) return node;

        if (node.isObject()) {
            // If the object has any automation-related fields, skip processing it entirely
            if (hasAutomationField(node)) return node;

            ObjectNode processedNode = mapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode child = entry.getValue();
                processedNode.set(fieldName, processIfNotAutomation(eventContext, child));
            });
            return processedNode;
        }

        if (node.isArray()) {
            ArrayNode processedArray = mapper.createArrayNode();
            for (JsonNode item : node) {
                processedArray.add(processIfNotAutomation(eventContext, item));
            }
            return processedArray;
        }

        if (node.isTextual()) {
            try {
                String processedText = templateProcessor.process(node.asText(), eventContext.getEventData());
                return new TextNode(processedText);
            } catch (IOException e) {
                log.error("Error processing template for text node: {}. Error: {}", node.asText(), e.getMessage());
                throw new AutomationEngineProcessingException(e);
            }
        }

        // For other types (numbers, booleans, etc.), leave them as is
        return node;
    }

    private boolean hasAutomationField(JsonNode node) {
        if (!node.isObject()) {
            return false;
        }
        for (String field : AUTOMATION_FIELDS) {
            if (node.has(field)) {
                return true;
            }
        }
        return false;
    }


    private JsonNode processOnlyString(EventContext eventContext, ResultContext resultContext) {
        JsonNode jsonNodeCopy = resultContext.getData();
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNodeCopy.fields(); it.hasNext(); ) {
            var entry = it.next();
            if (entry.getValue().isTextual()) {
                String valueStr = entry.getValue().asText();
                try {
                    String processedValue = templateProcessor.process(valueStr, eventContext.getEventData());
                    entry.setValue(mapper.getNodeFactory().textNode(processedValue));
                } catch (IOException e) {
                    log.error("Error processing template for key: {}. Error: {}", entry.getKey(), e.getMessage());
                    throw new AutomationEngineProcessingException(e);
                }
            }
        }
        return jsonNodeCopy;
    }

    public static class AutomationEngineProcessingException extends RuntimeException {
        public AutomationEngineProcessingException(Throwable cause) {
            super(cause);
        }
    }
}