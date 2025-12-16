package com.davidrandoll.automation.engine.spring.modules.actions.repeat;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
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
        if (node == null) return Collections.emptyList();

        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.forEach(n -> list.add(objectMapper.convertValue(n, Object.class)));
            return list;
        } else if (node.isTextual()) {
            String text = node.asText();
            try {
                JsonNode parsedNode = objectMapper.readTree(text);
                return convertToIterable(parsedNode);
            } catch (Exception e) {
                log.warn("Failed to parse forEach text as JSON array: {}", e.getMessage());
            }
        }

        // single value → singleton list
        return List.of(objectMapper.convertValue(node, Object.class));
    }
}