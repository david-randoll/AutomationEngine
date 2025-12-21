package com.davidrandoll.automation.engine.tracing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Trace entry for trigger evaluation operations.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TriggerTraceEntry extends BaseTraceEntry {
    /**
     * Whether the trigger was activated (returned true).
     */
    private boolean activated;
}
