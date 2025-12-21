package com.davidrandoll.automation.engine.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents nested trace entries for child operations.
 * Used when actions contain nested conditions, actions, etc. (e.g., ifThenElse action).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceChildren {
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
