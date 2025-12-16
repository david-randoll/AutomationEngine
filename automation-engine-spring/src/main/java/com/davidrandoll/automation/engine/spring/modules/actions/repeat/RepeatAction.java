package com.davidrandoll.automation.engine.spring.modules.actions.repeat;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RepeatAction extends PluggableAction<RepeatActionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public void doExecute(EventContext ec, RepeatActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return;
        for (int i = 0; i < ac.getCount(); i++) {
            processor.executeActions(ec, ac.getActions());
        }

        if (ac.hasWhileConditions()) {
            while (processor.allConditionsSatisfied(ec, ac.getWhileConditions())) {
                processor.executeActions(ec, ac.getActions());
            }
        }

        if (ac.hasUntilConditions()) {
            while (!processor.allConditionsSatisfied(ec, ac.getUntilConditions())) {
                processor.executeActions(ec, ac.getActions());
            }
        }

        if (ac.hasForEach()) {
            Iterable<?> items = convertToIterable(ac.getForEach());
            String variableName = ac.getAs();

            for (Object item : items) {
                ec.addMetadata(variableName, item);
                processor.executeActions(ec, ac.getActions());
                ec.removeMetadata(variableName);
            }
        }
    }

    private Iterable<?> convertToIterable(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return List.of();
        }

        // Case 1: already a JSON array
        if (node.isArray()) {
            return jsonArrayToList(node);
        }

        // Case 2: string that MAY contain JSON
        if (node.isTextual()) {
            JsonNode parsed = tryParseJson(node.asText());
            if (parsed != null) {
                return convertToIterable(parsed);
            }

            // Not JSON → treat as single value
            return List.of(node.asText());
        }

        // Case 3: object / number / boolean → single value
        return List.of(objectMapper.convertValue(node, Object.class));
    }

    private List<Object> jsonArrayToList(JsonNode arrayNode) {
        List<Object> list = new ArrayList<>(arrayNode.size());
        arrayNode.forEach(n -> list.add(objectMapper.convertValue(n, Object.class)));
        return list;
    }

    private JsonNode tryParseJson(String text) {
        String trimmed = text.trim();

        // Cheap structural guard
        if (!looksLikeJson(trimmed)) {
            return null;
        }

        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean looksLikeJson(String s) {
        return (s.startsWith("[") && s.endsWith("]")) ||
               (s.startsWith("{") && s.endsWith("}"));
    }
}