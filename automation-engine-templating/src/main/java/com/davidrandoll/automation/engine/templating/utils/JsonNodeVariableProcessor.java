package com.davidrandoll.automation.engine.templating.utils;

import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.interceptors.ResultTemplatingInterceptor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JsonNodeVariableProcessor {
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper mapper;
    private static final Set<String> AUTOMATION_FIELDS = Set.of("action", "variable", "condition", "trigger", "result");

    public Map<String, Object> processIfNotAutomation(Map<String, Object> eventData, Map<String, Object> map) {
        JsonNode node = mapper.valueToTree(map);
        node = processIfNotAutomation(eventData, node);
        return mapper.convertValue(node, new TypeReference<>() {
        });
    }

    public JsonNode processIfNotAutomation(Map<String, Object> eventData, JsonNode node) {
        if (node == null || node.isNull())
            return node;

        if (node.isObject()) {
            // If the object has any automation-related fields, skip processing it entirely
            if (hasAutomationField(node))
                return node;

            ObjectNode processedNode = mapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode child = entry.getValue();
                processedNode.set(fieldName, processIfNotAutomation(eventData, child));
            });
            return processedNode;
        }

        if (node.isArray()) {
            ArrayNode processedArray = mapper.createArrayNode();
            for (JsonNode item : node) {
                processedArray.add(processIfNotAutomation(eventData, item));
            }
            return processedArray;
        }

        if (node.isTextual()) {
            try {
                String processedText = templateProcessor.process(node.asText(), eventData);
                return parseStringToJsonNode(processedText);
            } catch (IOException e) {
                log.error("Error processing template for text node: {}. Error: {}", node.asText(), e.getMessage());
                throw new ResultTemplatingInterceptor.AutomationEngineProcessingException(e);
            }
        }

        // For other types (numbers, booleans, etc.), leave them as is
        return node;
    }

    /**
     * Parses a string value to the appropriate JsonNode type.
     * Attempts to parse as JSON to preserve numeric, boolean, array, and object
     * types.
     * Falls back to TextNode if the string is not valid JSON.
     *
     * @param value the string value to parse
     * @return JsonNode with the appropriate type (IntNode, BooleanNode, etc.) or
     *         TextNode if not parseable
     */
    private JsonNode parseStringToJsonNode(String value) {
        if (value == null) {
            return mapper.nullNode();
        }

        // Try to parse as JSON value to preserve type
        try {
            return mapper.readTree(value);
        } catch (Exception e) {
            // If it's not valid JSON, treat it as a text node
            return new TextNode(value);
        }
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

    public JsonNode processOnlyString(Map<String, Object> eventData, ResultContext resultContext) {
        JsonNode jsonNodeCopy = resultContext.getData();
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNodeCopy.fields(); it.hasNext();) {
            var entry = it.next();
            if (entry.getValue().isTextual()) {
                String valueStr = entry.getValue().asText();
                try {
                    String processedValue = templateProcessor.process(valueStr, eventData);
                    entry.setValue(parseStringToJsonNode(processedValue));
                } catch (IOException e) {
                    log.error("Error processing template for key: {}. Error: {}", entry.getKey(), e.getMessage());
                    throw new ResultTemplatingInterceptor.AutomationEngineProcessingException(e);
                }
            }
        }
        return jsonNodeCopy;
    }
}
