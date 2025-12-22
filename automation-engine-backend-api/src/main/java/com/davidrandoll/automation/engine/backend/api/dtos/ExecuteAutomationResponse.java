package com.davidrandoll.automation.engine.backend.api.dtos;

import com.davidrandoll.automation.engine.tracing.ExecutionTrace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for automation execution in the playground.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteAutomationResponse {
    /**
     * Whether the automation was executed (all conditions met).
     */
    private boolean executed;

    /**
     * The result value from the automation execution, if any.
     */
    private Object result;

    /**
     * The execution trace containing detailed timing and state information.
     */
    private ExecutionTrace trace;

    /**
     * Error message if execution failed.
     */
    private String error;
}
