package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.tracing.ExecutionTrace;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Execution interceptor that initializes tracing, captures the complete execution trace,
 * and attaches it to the AutomationResult's additional fields.
 * <p>
 * This interceptor should be the outermost interceptor in the chain to capture the complete
 * execution including all variables, triggers, conditions, actions, and results.
 * </p>
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class TracingExecutionInterceptor implements IAutomationExecutionInterceptor {
    private final boolean tracingEnabled;


    @Override
    public AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain) {
        // Check both global flag and per-automation flag
        boolean automationTracingEnabled = (boolean) automation.getOptions().getOrDefault("tracing", false);
        if (!tracingEnabled || !automationTracingEnabled) {
            log.debug("Tracing is disabled (global={}, automation={}), proceeding without trace capture", tracingEnabled, automationTracingEnabled);
            return chain.proceed(automation, context);
        }

        log.debug("Starting trace capture for automation: {}", automation.getAlias());

        // Initialize trace context and store in event context metadata
        TraceContext traceContext = TraceContext.getOrCreate(context, automation.getAlias());

        // Proceed with execution (all component interceptors will add their traces)
        AutomationResult result = chain.proceed(automation, context);

        // Complete the trace and get the final execution trace
        ExecutionTrace executionTrace = traceContext.complete();

        log.debug("Trace capture completed for automation: {}, executionId: {}",
                automation.getAlias(), executionTrace.getExecutionId());

        // Create new result with trace attached in additional fields
        Map<String, Object> additionalFields = new HashMap<>(result.getAdditionalFields());
        additionalFields.put(ExecutionTrace.TRACE_KEY, executionTrace);

        return AutomationResult.executedWithAdditionalFields(
                result.getAutomation(),
                result.getContext(),
                result.orElse(null),
                result.isExecuted(),
                additionalFields
        );
    }
}
