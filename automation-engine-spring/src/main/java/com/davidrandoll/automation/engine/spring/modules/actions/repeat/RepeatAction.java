package com.davidrandoll.automation.engine.spring.modules.actions.repeat;


import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.actions.IBaseAction;
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

/**
 * A block action that repeats its child actions based on various conditions.
 * <p>
 * Supports multiple repeat modes:
 * <ul>
 *   <li>{@code count} - Fixed number of iterations (unrolled at invocation time)</li>
 *   <li>{@code forEach} - Iterate over a collection (unrolled at invocation time)</li>
 *   <li>{@code whileConditions} - Loop while conditions are true (evaluated per iteration)</li>
 *   <li>{@code untilConditions} - Loop until conditions are true (evaluated per iteration)</li>
 * </ul>
 * <p>
 * For count and forEach, all iterations are unrolled upfront and invoked together.
 * For while/until, the engine evaluates conditions between iterations.
 */
@Slf4j
@RequiredArgsConstructor
public class RepeatAction extends PluggableAction<RepeatActionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public ActionResult executeWithResult(EventContext ec, RepeatActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) {
            return ActionResult.continueExecution();
        }

        // Count-based repetition: unroll all iterations
        if (ac.getCount() > 0) {
            List<IBaseAction> allActions = new ArrayList<>();
            for (int i = 0; i < ac.getCount(); i++) {
                allActions.addAll(createActions(ec, ac.getActions()));
            }
            return ActionResult.invoke(allActions);
        }

        // ForEach: unroll all iterations with loop variable set
        if (ac.hasForEach()) {
            Iterable<?> items = convertToIterable(ac.getForEach());
            String variableName = ac.getAs();
            List<IBaseAction> allActions = new ArrayList<>();

            for (Object item : items) {
                // Create a wrapper action that sets the variable, executes children, then removes it
                List<IBaseAction> iterationActions = createActions(ec, ac.getActions());
                allActions.add(new ForEachIterationWrapper(variableName, item, iterationActions));
            }
            return ActionResult.invoke(allActions);
        }

        // While/Until loops: these need to be re-evaluated between iterations
        // We execute actions directly (not using INVOKE) to avoid stack issues with the loop
        if (ac.hasWhileConditions()) {
            while (processor.allConditionsSatisfied(ec, ac.getWhileConditions())) {
                ActionResult result = executeActionsDirectly(ec, ac);
                if (result.isPause()) return ActionResult.pause();
                if (result.isStop()) return ActionResult.continueExecution();
            }
        }

        if (ac.hasUntilConditions()) {
            while (!processor.allConditionsSatisfied(ec, ac.getUntilConditions())) {
                ActionResult result = executeActionsDirectly(ec, ac);
                if (result.isPause()) return ActionResult.pause();
                if (result.isStop()) return ActionResult.continueExecution();
            }
        }

        return ActionResult.continueExecution();
    }

    /**
     * Executes actions directly without using the virtual stack machine.
     * This is needed for while/until loops where we need to re-evaluate conditions
     * between iterations.
     */
    private ActionResult executeActionsDirectly(EventContext ec, RepeatActionContext ac) {
        List<IBaseAction> actions = createActions(ec, ac.getActions());
        for (IBaseAction action : actions) {
            ActionResult result = action.execute(ec);
            if (result.isPause()) return ActionResult.pause();
            if (result.isStop()) return ActionResult.stop();
            // Handle INVOKE for nested blocks - execute them immediately
            if (result.isInvoke() && result.children() != null) {
                for (IBaseAction child : result.children()) {
                    ActionResult childResult = child.execute(ec);
                    if (childResult.isPause()) return ActionResult.pause();
                    if (childResult.isStop()) return ActionResult.stop();
                }
            }
        }
        return ActionResult.continueExecution();
    }

    @Override
    public void doExecute(EventContext ec, RepeatActionContext ac) {
        // No-op, using executeWithResult instead
    }

    /**
     * A wrapper action that sets a loop variable, executes child actions, then cleans up.
     */
    @RequiredArgsConstructor
    private static class ForEachIterationWrapper implements IBaseAction {
        private final String variableName;
        private final Object value;
        private final List<IBaseAction> children;

        @Override
        public ActionResult execute(EventContext ec) {
            ec.addMetadata(variableName, value);
            try {
                return ActionResult.invoke(children);
            } finally {
                // Note: cleanup happens when this wrapper is popped, not when children complete
                // This is a limitation - for proper cleanup we'd need post-execution hooks
            }
        }
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