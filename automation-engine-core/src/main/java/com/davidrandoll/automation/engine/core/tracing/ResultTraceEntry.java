package com.davidrandoll.automation.engine.core.tracing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Trace entry for result computation operations.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResultTraceEntry extends BaseTraceEntry {
    /**
     * The computed result value.
     */
    private Object result;
}
