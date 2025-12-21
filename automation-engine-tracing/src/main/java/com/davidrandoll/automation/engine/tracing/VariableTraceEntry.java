package com.davidrandoll.automation.engine.tracing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Trace entry for variable resolution operations.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VariableTraceEntry extends BaseTraceEntry {
}
