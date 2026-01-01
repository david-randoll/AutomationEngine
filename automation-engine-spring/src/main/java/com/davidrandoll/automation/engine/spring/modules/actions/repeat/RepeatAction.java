package com.davidrandoll.automation.engine.spring.modules.actions.repeat;


import com.davidrandoll.automation.engine.core.actions.ActionResult;
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
    public ActionResult executeWithResult(EventContext ec, RepeatActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return ActionResult.CONTINUE;
        for (int i = 0; i < ac.getCount(); i++) {
            ActionResult result = processor.executeActions(ec, ac.getActions());
            if (result == ActionResult.PAUSE) return ActionResult.PAUSE;
            if (result == ActionResult.STOP) return ActionResult.CONTINUE;
        }

        if (ac.hasWhileConditions()) {
            while (processor.allConditionsSatisfied(ec, ac.getWhileConditions())) {
                ActionResult result = processor.executeActions(ec, ac.getActions());
                if (result == ActionResult.PAUSE) return ActionResult.PAUSE;
                if (result == ActionResult.STOP) return ActionResult.CONTINUE;
            }
        }

        if (ac.hasUntilConditions()) {
            while (!processor.allConditionsSatisfied(ec, ac.getUntilConditions())) {
                ActionResult result = processor.executeActions(ec, ac.getActions());
                if (result == ActionResult.PAUSE) return ActionResult.PAUSE;
                if (result == ActionResult.STOP) return ActionResult.CONTINUE;
            }
        }

        if (ac.hasForEach()) {
            Iterable<?> items = convertToIterable(ac.getForEach());
            String variableName = ac.getAs();

            for (Object item : items) {
                ec.addMetadata(variableName, item);
                ActionResult result = processor.executeActions(ec, ac.getActions());
                ec.removeMetadata(variableName);
                if (result == ActionResult.PAUSE) return ActionResult.PAUSE;
                if (result == ActionResult.STOP) return ActionResult.CONTINUE;
            }
        }
        return ActionResult.CONTINUE;
    }

    @Override
    public void doExecute(EventContext ec, RepeatActionContext ac) {
        // No-op, using executeWithResult instead
    }

    private Iterable<?> convertToIterable(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return Collections.emptyList();

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
                // not a parsable JSON, treat as single value
            }

            if (text.startsWith("[") && text.endsWith("]")) {
                String content = text.substring(1, text.length() - 1).trim();
                if (ObjectUtils.isEmpty(content)) return Collections.emptyList();
                try {
                    JsonNode parsedNode = objectMapper.readTree(text);
                    return convertToIterable(parsedNode);
                } catch (Exception e) {
                    // Not valid JSON, treat as toString() format
                }

                // Parse as toString() format (no quotes around strings)
                String[] items = content.split("\\s*,\\s*");
                log.debug("Parsed forEach toString() format into {} items", items.length);
                return java.util.Arrays.asList(items);
            }
        }

        // single value → singleton list
        return List.of(objectMapper.convertValue(node, Object.class));
    }
}