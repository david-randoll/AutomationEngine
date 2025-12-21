package com.davidrandoll.automation.engine.tracing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Trace entry for condition evaluation operations.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConditionTraceEntry extends BaseTraceEntry {
    /**
     * Whether the condition was satisfied (returned true).
     */
    private boolean satisfied;
}
