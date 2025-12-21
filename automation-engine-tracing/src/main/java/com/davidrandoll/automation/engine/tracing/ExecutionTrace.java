package com.davidrandoll.automation.engine.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Complete execution trace for an automation run.
 * Contains all trace entries for variables, triggers, conditions, actions, and result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionTrace {
    /**
     * Unique identifier for this execution.
     */
    @Builder.Default
    private String executionId = UUID.randomUUID().toString();

    /**
     * The alias of the automation that was executed.
     */
    private String alias;

    /**
     * Timestamp when the automation execution started (milliseconds since epoch).
     */
    private long startedAt;

    /**
     * Timestamp when the automation execution finished (milliseconds since epoch).
     */
    private long finishedAt;

    /**
     * The trace data containing all component executions.
     */
    @Builder.Default
    private TraceData trace = new TraceData();

    /**
     * Inner class containing the categorized trace entries.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceData {
        @Builder.Default
        private List<VariableTraceEntry> variables = new ArrayList<>();

        @Builder.Default
        private List<TriggerTraceEntry> triggers = new ArrayList<>();

        @Builder.Default
        private List<ConditionTraceEntry> conditions = new ArrayList<>();

        @Builder.Default
        private List<ActionTraceEntry> actions = new ArrayList<>();

        private ResultTraceEntry result;
    }

    /**
     * The key used to store the trace in AutomationResult's additionalFields.
     */
    public static final String TRACE_KEY = "trace";
}
