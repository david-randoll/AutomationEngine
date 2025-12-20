package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for building hierarchical execution trees from trace data.
 * Converts flat trace lists into nested execution paths suitable for UI
 * rendering.
 */
@Slf4j
public final class TraceTreeBuilder {

    private TraceTreeBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Build a hierarchical execution tree from automation result.
     * 
     * @param automation The automation that was executed
     * @param context    The event context with trace data
     * @param result     The automation execution result
     * @return Root ExecutionNode representing the entire automation execution
     */
    public static ExecutionNode buildExecutionTree(Automation automation, EventContext context,
            AutomationResult result) {
        Long startNanos = (Long) context.getMetadata().get(TraceConstants.TRACE_AUTOMATION_START);
        Long endNanos = (Long) context.getMetadata().get(TraceConstants.TRACE_AUTOMATION_END);
        Long durationNanos = (Long) context.getMetadata().get(TraceConstants.TRACE_AUTOMATION_DURATION_NANOS);

        // Build root automation node
        ExecutionNode root = ExecutionNode.builder()
                .type(TraceConstants.NODE_TYPE_AUTOMATION)
                .name(automation.getAlias())
                .status(result.isExecuted() ? TraceConstants.STATUS_EXECUTED : TraceConstants.STATUS_SKIPPED)
                .startTimeNanos(startNanos)
                .endTimeNanos(endNanos)
                .durationNanos(durationNanos)
                .durationMillis(durationNanos != null ? durationNanos / 1_000_000.0 : null)
                .metadata(new LinkedHashMap<>())
                .build();

        // Add trace ID to root metadata
        String traceId = (String) context.getMetadata().get(TraceConstants.TRACE_ID);
        if (traceId != null) {
            root.getMetadata().put("traceId", traceId);
        }

        // Add context snapshots if available
        Object snapshotBefore = context.getMetadata().get(TraceConstants.TRACE_CONTEXT_SNAPSHOT_BEFORE);
        if (snapshotBefore != null) {
            root.getMetadata().put("contextSnapshotBefore", snapshotBefore);
        }

        Object snapshotAfter = context.getMetadata().get(TraceConstants.TRACE_CONTEXT_SNAPSHOT_AFTER);
        if (snapshotAfter != null) {
            root.getMetadata().put("contextSnapshotAfter", snapshotAfter);
        }

        // Build phases as children
        ExecutionNode variablesPhase = buildVariablesPhase(context);
        if (variablesPhase.hasChildren()) {
            root.addChild(variablesPhase);
        }

        ExecutionNode triggersPhase = buildTriggersPhase(context);
        if (triggersPhase.hasChildren()) {
            root.addChild(triggersPhase);

            // Check if any trigger was activated
            boolean anyActivated = triggersPhase.getChildren().stream()
                    .anyMatch(node -> TraceConstants.STATUS_ACTIVATED.equals(node.getStatus()));

            if (anyActivated) {
                // Only add conditions and actions if triggers were activated
                ExecutionNode conditionsPhase = buildConditionsPhase(context);
                if (conditionsPhase.hasChildren()) {
                    root.addChild(conditionsPhase);

                    // Check if all conditions satisfied
                    boolean allSatisfied = conditionsPhase.getChildren().stream()
                            .allMatch(node -> TraceConstants.STATUS_SATISFIED.equals(node.getStatus()));

                    if (allSatisfied) {
                        // Only add actions if conditions were satisfied
                        ExecutionNode actionsPhase = buildActionsPhase(context);
                        if (actionsPhase.hasChildren()) {
                            root.addChild(actionsPhase);
                        }

                        // Add result phase if executed
                        ExecutionNode resultPhase = buildResultPhase(context);
                        if (resultPhase != null) {
                            root.addChild(resultPhase);
                        }
                    } else {
                        // Conditions not satisfied - add skipped message
                        conditionsPhase.getMetadata().put("message", "Conditions not satisfied - automation skipped");
                    }
                } else {
                    // No conditions means they're automatically satisfied
                    ExecutionNode actionsPhase = buildActionsPhase(context);
                    if (actionsPhase.hasChildren()) {
                        root.addChild(actionsPhase);
                    }

                    ExecutionNode resultPhase = buildResultPhase(context);
                    if (resultPhase != null) {
                        root.addChild(resultPhase);
                    }
                }
            } else {
                // No triggers activated - add skipped message
                triggersPhase.getMetadata().put("message", "No triggers activated - automation skipped");
            }
        }

        return root;
    }

    /**
     * Build the variables phase node with all variable resolution traces as
     * children.
     */
    private static ExecutionNode buildVariablesPhase(EventContext context) {
        List<Map<String, Object>> variables = TraceDataCollector.getTraceList(context, TraceConstants.TRACE_VARIABLES);

        ExecutionNode phase = ExecutionNode.builder()
                .type(TraceConstants.NODE_TYPE_PHASE)
                .name(TraceConstants.PHASE_VARIABLES)
                .status(TraceConstants.STATUS_EXECUTED)
                .metadata(new LinkedHashMap<>())
                .build();

        phase.getMetadata().put("count", variables.size());

        for (Map<String, Object> varTrace : variables) {
            ExecutionNode varNode = ExecutionNode.builder()
                    .type(TraceConstants.NODE_TYPE_VARIABLE)
                    .name((String) varTrace.get(TraceConstants.FIELD_ALIAS))
                    .status(TraceConstants.STATUS_EXECUTED)
                    .startTimeNanos((Long) varTrace.get(TraceConstants.FIELD_START_TIME_NANOS))
                    .endTimeNanos((Long) varTrace.get(TraceConstants.FIELD_END_TIME_NANOS))
                    .durationNanos((Long) varTrace.get(TraceConstants.FIELD_DURATION_NANOS))
                    .durationMillis(calculateMillis((Long) varTrace.get(TraceConstants.FIELD_DURATION_NANOS)))
                    .metadata(new LinkedHashMap<>())
                    .build();

            // Add before/after values
            if (varTrace.containsKey(TraceConstants.FIELD_BEFORE_VALUE)) {
                varNode.getMetadata().put("beforeValue", varTrace.get(TraceConstants.FIELD_BEFORE_VALUE));
            }
            if (varTrace.containsKey(TraceConstants.FIELD_AFTER_VALUE)) {
                varNode.getMetadata().put("afterValue", varTrace.get(TraceConstants.FIELD_AFTER_VALUE));
            }
            if (varTrace.containsKey(TraceConstants.FIELD_TYPE)) {
                varNode.getMetadata().put("contextType", varTrace.get(TraceConstants.FIELD_TYPE));
            }

            phase.addChild(varNode);
        }

        return phase;
    }

    /**
     * Build the triggers phase node with all trigger evaluation traces as children.
     */
    private static ExecutionNode buildTriggersPhase(EventContext context) {
        List<Map<String, Object>> triggers = TraceDataCollector.getTraceList(context, TraceConstants.TRACE_TRIGGERS);

        ExecutionNode phase = ExecutionNode.builder()
                .type(TraceConstants.NODE_TYPE_PHASE)
                .name(TraceConstants.PHASE_TRIGGERS)
                .status(TraceConstants.STATUS_EXECUTED)
                .metadata(new LinkedHashMap<>())
                .build();

        long activatedCount = triggers.stream()
                .filter(t -> Boolean.TRUE.equals(t.get(TraceConstants.FIELD_ACTIVATED)))
                .count();

        phase.getMetadata().put("count", triggers.size());
        phase.getMetadata().put("activatedCount", activatedCount);

        for (Map<String, Object> triggerTrace : triggers) {
            boolean activated = Boolean.TRUE.equals(triggerTrace.get(TraceConstants.FIELD_ACTIVATED));

            ExecutionNode triggerNode = ExecutionNode.builder()
                    .type(TraceConstants.NODE_TYPE_TRIGGER)
                    .name((String) triggerTrace.get(TraceConstants.FIELD_ALIAS))
                    .status(activated ? TraceConstants.STATUS_ACTIVATED : TraceConstants.STATUS_NOT_ACTIVATED)
                    .startTimeNanos((Long) triggerTrace.get(TraceConstants.FIELD_START_TIME_NANOS))
                    .endTimeNanos((Long) triggerTrace.get(TraceConstants.FIELD_END_TIME_NANOS))
                    .durationNanos((Long) triggerTrace.get(TraceConstants.FIELD_DURATION_NANOS))
                    .durationMillis(calculateMillis((Long) triggerTrace.get(TraceConstants.FIELD_DURATION_NANOS)))
                    .metadata(new LinkedHashMap<>())
                    .build();

            if (triggerTrace.containsKey(TraceConstants.FIELD_TYPE)) {
                triggerNode.getMetadata().put("contextType", triggerTrace.get(TraceConstants.FIELD_TYPE));
            }

            phase.addChild(triggerNode);
        }

        return phase;
    }

    /**
     * Build the conditions phase node with all condition evaluation traces as
     * children.
     */
    private static ExecutionNode buildConditionsPhase(EventContext context) {
        List<Map<String, Object>> conditions = TraceDataCollector.getTraceList(context,
                TraceConstants.TRACE_CONDITIONS);

        ExecutionNode phase = ExecutionNode.builder()
                .type(TraceConstants.NODE_TYPE_PHASE)
                .name(TraceConstants.PHASE_CONDITIONS)
                .status(TraceConstants.STATUS_EXECUTED)
                .metadata(new LinkedHashMap<>())
                .build();

        boolean allSatisfied = conditions.stream()
                .allMatch(c -> Boolean.TRUE.equals(c.get(TraceConstants.FIELD_SATISFIED)));

        phase.getMetadata().put("count", conditions.size());
        phase.getMetadata().put("allSatisfied", allSatisfied);

        for (Map<String, Object> conditionTrace : conditions) {
            boolean satisfied = Boolean.TRUE.equals(conditionTrace.get(TraceConstants.FIELD_SATISFIED));

            ExecutionNode conditionNode = ExecutionNode.builder()
                    .type(TraceConstants.NODE_TYPE_CONDITION)
                    .name((String) conditionTrace.get(TraceConstants.FIELD_ALIAS))
                    .status(satisfied ? TraceConstants.STATUS_SATISFIED : TraceConstants.STATUS_NOT_SATISFIED)
                    .startTimeNanos((Long) conditionTrace.get(TraceConstants.FIELD_START_TIME_NANOS))
                    .endTimeNanos((Long) conditionTrace.get(TraceConstants.FIELD_END_TIME_NANOS))
                    .durationNanos((Long) conditionTrace.get(TraceConstants.FIELD_DURATION_NANOS))
                    .durationMillis(calculateMillis((Long) conditionTrace.get(TraceConstants.FIELD_DURATION_NANOS)))
                    .metadata(new LinkedHashMap<>())
                    .build();

            if (conditionTrace.containsKey(TraceConstants.FIELD_TYPE)) {
                conditionNode.getMetadata().put("contextType", conditionTrace.get(TraceConstants.FIELD_TYPE));
            }

            phase.addChild(conditionNode);
        }

        return phase;
    }

    /**
     * Build the actions phase node with all action execution traces as children.
     */
    private static ExecutionNode buildActionsPhase(EventContext context) {
        List<Map<String, Object>> actions = TraceDataCollector.getTraceList(context, TraceConstants.TRACE_ACTIONS);

        ExecutionNode phase = ExecutionNode.builder()
                .type(TraceConstants.NODE_TYPE_PHASE)
                .name(TraceConstants.PHASE_ACTIONS)
                .status(TraceConstants.STATUS_EXECUTED)
                .metadata(new LinkedHashMap<>())
                .build();

        long failedCount = actions.stream()
                .filter(a -> a.containsKey(TraceConstants.FIELD_EXCEPTION))
                .count();

        phase.getMetadata().put("count", actions.size());
        phase.getMetadata().put("failedCount", failedCount);

        for (Map<String, Object> actionTrace : actions) {
            boolean hasException = actionTrace.containsKey(TraceConstants.FIELD_EXCEPTION);

            ExecutionNode actionNode = ExecutionNode.builder()
                    .type(TraceConstants.NODE_TYPE_ACTION)
                    .name((String) actionTrace.get(TraceConstants.FIELD_ALIAS))
                    .status(hasException ? TraceConstants.STATUS_FAILED : TraceConstants.STATUS_SUCCESS)
                    .startTimeNanos((Long) actionTrace.get(TraceConstants.FIELD_START_TIME_NANOS))
                    .endTimeNanos((Long) actionTrace.get(TraceConstants.FIELD_END_TIME_NANOS))
                    .durationNanos((Long) actionTrace.get(TraceConstants.FIELD_DURATION_NANOS))
                    .durationMillis(calculateMillis((Long) actionTrace.get(TraceConstants.FIELD_DURATION_NANOS)))
                    .metadata(new LinkedHashMap<>())
                    .build();

            if (actionTrace.containsKey(TraceConstants.FIELD_TYPE)) {
                actionNode.getMetadata().put("contextType", actionTrace.get(TraceConstants.FIELD_TYPE));
            }

            if (hasException) {
                actionNode.getMetadata().put("exception", actionTrace.get(TraceConstants.FIELD_EXCEPTION));
                actionNode.getMetadata().put("exceptionMessage",
                        actionTrace.get(TraceConstants.FIELD_EXCEPTION_MESSAGE));
            }

            phase.addChild(actionNode);
        }

        return phase;
    }

    /**
     * Build the result phase node.
     */
    private static ExecutionNode buildResultPhase(EventContext context) {
        Object resultTrace = context.getMetadata().get(TraceConstants.TRACE_RESULT);
        if (resultTrace == null) {
            return null;
        }

        Map<String, Object> resultMap = (resultTrace instanceof Map)
                ? (Map<String, Object>) resultTrace
                : new LinkedHashMap<>();

        ExecutionNode resultNode = ExecutionNode.builder()
                .type(TraceConstants.NODE_TYPE_RESULT)
                .name(TraceConstants.PHASE_RESULT)
                .status(TraceConstants.STATUS_EXECUTED)
                .startTimeNanos((Long) resultMap.get(TraceConstants.FIELD_START_TIME_NANOS))
                .endTimeNanos((Long) resultMap.get(TraceConstants.FIELD_END_TIME_NANOS))
                .durationNanos((Long) resultMap.get(TraceConstants.FIELD_DURATION_NANOS))
                .durationMillis(calculateMillis((Long) resultMap.get(TraceConstants.FIELD_DURATION_NANOS)))
                .metadata(new LinkedHashMap<>())
                .build();

        if (resultMap.containsKey(TraceConstants.FIELD_RESULT_TYPE)) {
            resultNode.getMetadata().put("resultType", resultMap.get(TraceConstants.FIELD_RESULT_TYPE));
        }

        if (resultMap.containsKey(TraceConstants.FIELD_RESULT_VALUE)) {
            resultNode.getMetadata().put("resultValue", resultMap.get(TraceConstants.FIELD_RESULT_VALUE));
        }

        return resultNode;
    }

    /**
     * Calculate milliseconds from nanoseconds.
     */
    private static Double calculateMillis(Long nanos) {
        return nanos != null ? nanos / 1_000_000.0 : null;
    }
}
