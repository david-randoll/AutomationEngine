package com.davidrandoll.automation.engine.templating.utils;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.templating.AETemplatingProperties;
import com.davidrandoll.automation.engine.templating.ContextOption;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.interceptors.AutomationOptionsInterceptor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JsonNodeVariableProcessor {
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper mapper;
    private final AETemplatingProperties properties;
    private static final Set<String> AUTOMATION_FIELDS = Set.of("action", "variable", "condition", "trigger", "result");

    public Map<String, Object> processIfNotAutomation(Map<String, Object> eventData, Map<String, Object> map) {
        String templatingType = getTemplatingType(null, (Map<String, Object>) map.get("options"));
        return processIfNotAutomation(eventData, map, templatingType);
    }

    public Map<String, Object> processIfNotAutomation(Map<String, Object> eventData, Map<String, Object> map,
                                                      String templatingType) {
        JsonNode node = mapper.valueToTree(map);
        node = processIfNotAutomation(eventData, node, templatingType);
        return mapper.convertValue(node, new TypeReference<>() {
        });
    }

    public JsonNode processIfNotAutomation(Map<String, Object> eventData, JsonNode node) {
        return processIfNotAutomation(eventData, node, properties.getDefaultEngine());
    }

    public JsonNode processIfNotAutomation(Map<String, Object> eventData, JsonNode node, String templatingType) {
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
                if ("options".equals(fieldName)) {
                    processedNode.set(fieldName, child);
                } else {
                    processedNode.set(fieldName, processIfNotAutomation(eventData, child, templatingType));
                }
            });
            return processedNode;
        }

        if (node.isArray()) {
            ArrayNode processedArray = mapper.createArrayNode();
            for (JsonNode item : node) {
                processedArray.add(processIfNotAutomation(eventData, item, templatingType));
            }
            return processedArray;
        }

        if (node.isTextual()) {
            Object processedValue = templateProcessor.process(node.asText(), eventData, templatingType);
            return convertObjectToJsonNode(processedValue);
        }

        // For other types (numbers, booleans, etc.), leave them as is
        return node;
    }

    /**
     * Converts an Object to the appropriate JsonNode type.
     * Handles various types including String, List, Map, primitives, etc.
     * For strings, attempts to parse as JSON to preserve type information.
     *
     * @param value the object value to convert
     * @return JsonNode with the appropriate type
     */
    private JsonNode convertObjectToJsonNode(Object value) {
        if (value == null) {
            return mapper.nullNode();
        }

        // If it's a String, try to parse it as JSON first (for backward compatibility
        // with Pebble)
        if (value instanceof String) {
            return parseStringToJsonNode((String) value);
        }

        // For non-string objects (like arrays, maps, etc. from SpEL), convert directly
        return mapper.valueToTree(value);
    }

    /**
     * Parses a string value to the appropriate JsonNode type.
     * Attempts to parse as JSON to preserve numeric, boolean, array, and object
     * types.
     * Falls back to TextNode if the string is not valid JSON.
     *
     * @param value the string value to parse
     * @return JsonNode with the appropriate type (IntNode, BooleanNode, etc.) or
     * TextNode if not parseable
     */
    private JsonNode parseStringToJsonNode(String value) {
        if (value == null) {
            return mapper.nullNode();
        } else if (ObjectUtils.isEmpty(value)) {
            return new TextNode(value);
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
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNodeCopy.fields(); it.hasNext(); ) {
            var entry = it.next();
            if (entry.getValue().isTextual()) {
                String valueStr = entry.getValue().asText();
                try {
                    Object processedValue = templateProcessor.process(valueStr, eventData);
                    entry.setValue(convertObjectToJsonNode(processedValue));
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

    public String getTemplatingType(Map<String, Object> options) {
        return getTemplatingType(null, options);
    }

    public String getTemplatingType(EventContext eventContext, Map<String, Object> options) {
        // 1. Block level (actions, conditions, triggers, result, variable)
        String blockType = getFromOptions(options);
        if (!ObjectUtils.isEmpty(blockType)) return blockType;

        // 2. Automation level
        if (eventContext != null) {
            Object automationOptions = eventContext.getMetadata(AutomationOptionsInterceptor.AUTOMATION_OPTIONS_KEY);
            if (automationOptions instanceof Map<?, ?> automationOptionsMap) {
                String automationType = getFromOptions(automationOptionsMap);
                if (!ObjectUtils.isEmpty(automationType)) return automationType;
            }
        }

        // 3. Default from configuration properties
        return properties.getDefaultEngine();
    }

    private String getFromOptions(Map<?, ?> options) {
        if (ObjectUtils.isEmpty(options))
            return null;
        try {
            ContextOption contextOption = mapper.convertValue(options, ContextOption.class);
            if (contextOption != null && !ObjectUtils.isEmpty(contextOption.getTemplatingType())) {
                return contextOption.getTemplatingType();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}