package com.davidrandoll.automation.engine.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a snapshot of the state at a point in time during execution.
 * Contains both the event data and the context data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceSnapshot {
    /**
     * Snapshot of the event data (from EventContext.getEventData()).
     */
    private Map<String, Object> eventSnapshot;

    /**
     * Snapshot of the context data (from ActionContext.getData(), etc.).
     */
    private Map<String, Object> contextSnapshot;
}
