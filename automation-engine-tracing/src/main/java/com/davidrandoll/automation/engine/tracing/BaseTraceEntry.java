package com.davidrandoll.automation.engine.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all trace entries.
 * Contains common fields for timing and snapshots.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseTraceEntry {
    /**
     * The type of the component (e.g., "loggerAction", "expression", "alwaysTrueTrigger").
     */
    private String type;

    /**
     * The alias of the component, if specified.
     */
    private String alias;

    /**
     * Timestamp when execution started (milliseconds since epoch).
     */
    private long startedAt;

    /**
     * Timestamp when execution finished (milliseconds since epoch).
     */
    private long finishedAt;

    /**
     * Snapshot of state before execution.
     */
    private TraceSnapshot before;

    /**
     * Snapshot of state after execution.
     */
    private TraceSnapshot after;

    /**
     * Nested children entries for composite operations.
     */
    private TraceChildren children;

    /**
     * Captured log messages produced during execution of this component.
     */
    @Builder.Default
    private List<String> logs = new ArrayList<>();
}
