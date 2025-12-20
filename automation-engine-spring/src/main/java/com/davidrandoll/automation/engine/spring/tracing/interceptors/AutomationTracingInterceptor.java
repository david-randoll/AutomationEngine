package com.davidrandoll.automation.engine.spring.tracing.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
import com.davidrandoll.automation.engine.spring.tracing.ExecutionNode;
import com.davidrandoll.automation.engine.spring.tracing.TraceConstants;
import com.davidrandoll.automation.engine.spring.tracing.TraceDataCollector;
import com.davidrandoll.automation.engine.spring.tracing.TraceTreeBuilder;
import com.davidrandoll.automation.engine.spring.tracing.TracingConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.*;

/**
 * Top-level interceptor for automation execution tracing.
 * Generates trace IDs, captures overall execution timing, and aggregates child
 * traces.
 * Extracts trace data and attaches to AutomationResult.additionalFields.
 */
@Slf4j
@RequiredArgsConstructor
@Order(100) // Execute after logging/transaction interceptors but before others
public class AutomationTracingInterceptor implements IAutomationExecutionInterceptor {

    private final TracingConfigurationProperties config;

    @Override
    public AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain) {
        // Generate trace ID and mark tracing as enabled
        String traceId = UUID.randomUUID().toString();
        long startNanos = System.nanoTime();

        context.addMetadata(TraceConstants.TRACE_ID, traceId);
        context.addMetadata(TraceConstants.TRACE_AUTOMATION_START, startNanos);

        // Capture context snapshot before execution
        if (config.isIncludeContextSnapshots()
                && config.getSnapshotMode() != TracingConfigurationProperties.SnapshotMode.NONE) {
            boolean keysOnly = config.getSnapshotMode() == TracingConfigurationProperties.SnapshotMode.KEYS_ONLY;
            Map<String, Object> snapshotBefore = TraceDataCollector.captureContextSnapshot(context, keysOnly);
            context.addMetadata(TraceConstants.TRACE_CONTEXT_SNAPSHOT_BEFORE, snapshotBefore);
        }

        log.debug("Starting traced automation execution: {} (traceId: {})", automation.getAlias(), traceId);

        AutomationResult result;
        try {
            // Execute automation (component interceptors will add their traces)
            result = chain.proceed(automation, context);

            long endNanos = System.nanoTime();
            context.addMetadata(TraceConstants.TRACE_AUTOMATION_END, endNanos);
            context.addMetadata(TraceConstants.TRACE_AUTOMATION_DURATION_NANOS, endNanos - startNanos);
            context.addMetadata(TraceConstants.TRACE_EXECUTED, result.isExecuted());

            // Capture context snapshot after execution
            if (config.isIncludeContextSnapshots()
                    && config.getSnapshotMode() != TracingConfigurationProperties.SnapshotMode.NONE) {
                boolean keysOnly = config.getSnapshotMode() == TracingConfigurationProperties.SnapshotMode.KEYS_ONLY;
                Map<String, Object> snapshotAfter = TraceDataCollector.captureContextSnapshot(context, keysOnly);
                context.addMetadata(TraceConstants.TRACE_CONTEXT_SNAPSHOT_AFTER, snapshotAfter);
            }

            // Extract and build comprehensive trace data
            Map<String, Object> traceData = buildTraceData(automation, context, result);

            // Create result with trace data in additionalFields
            result = AutomationResult.executedWithAdditionalFields(
                    automation,
                    context,
                    result.getResult().orElse(null),
                    result.isExecuted(),
                    Collections.singletonMap("trace", traceData));

            // Clear trace data from context if configured
            if (config.isClearAfterExtraction()) {
                clearTraceMetadata(context);
            }

            log.debug("Completed traced automation execution: {} in {}ms (traceId: {})",
                    automation.getAlias(),
                    (endNanos - startNanos) / 1_000_000.0,
                    traceId);

        } catch (Exception e) {
            long endNanos = System.nanoTime();
            context.addMetadata(TraceConstants.TRACE_AUTOMATION_END, endNanos);
            context.addMetadata(TraceConstants.TRACE_AUTOMATION_DURATION_NANOS, endNanos - startNanos);

            log.error("Traced automation execution failed: {} (traceId: {})", automation.getAlias(), traceId, e);
            throw e;
        }

        return result;
    }

    /**
     * Build comprehensive trace data from EventContext metadata.
     */
    private Map<String, Object> buildTraceData(Automation automation, EventContext context, AutomationResult result) {
        Map<String, Object> trace = new LinkedHashMap<>();

        // Basic info
        trace.put("traceId", context.getMetadata().get(TraceConstants.TRACE_ID));
        trace.put("automationAlias", automation.getAlias());
        trace.put("executed", result.isExecuted());

        // Timing
        Long startNanos = (Long) context.getMetadata().get(TraceConstants.TRACE_AUTOMATION_START);
        Long endNanos = (Long) context.getMetadata().get(TraceConstants.TRACE_AUTOMATION_END);
        Long durationNanos = (Long) context.getMetadata().get(TraceConstants.TRACE_AUTOMATION_DURATION_NANOS);

        if (startNanos != null && endNanos != null && durationNanos != null) {
            Map<String, Object> timing = new LinkedHashMap<>();
            timing.put("startNanos", startNanos);
            timing.put("endNanos", endNanos);
            timing.put("durationNanos", durationNanos);
            timing.put("durationMillis", durationNanos / 1_000_000.0);
            trace.put("timing", timing);
        }

        // Build hierarchical execution tree
        ExecutionNode executionTree = TraceTreeBuilder.buildExecutionTree(automation, context, result);
        trace.put(TraceConstants.EXECUTION_TREE, executionTree.toMap());

        // Also keep flat structure for backward compatibility (can be removed later if
        // not needed)
        List<Map<String, Object>> variables = TraceDataCollector.getTraceList(context, TraceConstants.TRACE_VARIABLES);
        if (!variables.isEmpty()) {
            trace.put("variables", variables);
        }

        List<Map<String, Object>> triggers = TraceDataCollector.getTraceList(context, TraceConstants.TRACE_TRIGGERS);
        if (!triggers.isEmpty()) {
            trace.put("triggers", triggers);
            // Add summary: which trigger(s) activated
            long activatedCount = triggers.stream()
                    .filter(t -> Boolean.TRUE.equals(t.get(TraceConstants.FIELD_ACTIVATED)))
                    .count();
            trace.put("triggersActivated", activatedCount);
        }

        List<Map<String, Object>> conditions = TraceDataCollector.getTraceList(context,
                TraceConstants.TRACE_CONDITIONS);
        if (!conditions.isEmpty()) {
            trace.put("conditions", conditions);
            // Add summary: all conditions satisfied?
            boolean allSatisfied = conditions.stream()
                    .allMatch(c -> Boolean.TRUE.equals(c.get(TraceConstants.FIELD_SATISFIED)));
            trace.put("allConditionsSatisfied", allSatisfied);
        }

        List<Map<String, Object>> actions = TraceDataCollector.getTraceList(context, TraceConstants.TRACE_ACTIONS);
        if (!actions.isEmpty()) {
            trace.put("actions", actions);
            // Add summary: any action failures?
            long failedCount = actions.stream()
                    .filter(a -> a.containsKey(TraceConstants.FIELD_EXCEPTION))
                    .count();
            trace.put("actionsWithExceptions", failedCount);
        }

        Object resultTrace = context.getMetadata().get(TraceConstants.TRACE_RESULT);
        if (resultTrace != null) {
            trace.put("result", resultTrace);
        }

        // Context snapshots
        if (config.isIncludeContextSnapshots()) {
            Object snapshotBefore = context.getMetadata().get(TraceConstants.TRACE_CONTEXT_SNAPSHOT_BEFORE);
            if (snapshotBefore != null) {
                trace.put("contextSnapshotBefore", snapshotBefore);
            }

            Object snapshotAfter = context.getMetadata().get(TraceConstants.TRACE_CONTEXT_SNAPSHOT_AFTER);
            if (snapshotAfter != null) {
                trace.put("contextSnapshotAfter", snapshotAfter);
            }
        }

        return trace;
    }

    /**
     * Clear trace metadata from EventContext to prevent memory leaks.
     */
    private void clearTraceMetadata(EventContext context) {
        context.removeMetadata(TraceConstants.TRACE_ID);
        context.removeMetadata(TraceConstants.TRACE_AUTOMATION_START);
        context.removeMetadata(TraceConstants.TRACE_AUTOMATION_END);
        context.removeMetadata(TraceConstants.TRACE_AUTOMATION_DURATION_NANOS);
        context.removeMetadata(TraceConstants.TRACE_EXECUTED);
        context.removeMetadata(TraceConstants.TRACE_VARIABLES);
        context.removeMetadata(TraceConstants.TRACE_TRIGGERS);
        context.removeMetadata(TraceConstants.TRACE_CONDITIONS);
        context.removeMetadata(TraceConstants.TRACE_ACTIONS);
        context.removeMetadata(TraceConstants.TRACE_RESULT);
        context.removeMetadata(TraceConstants.TRACE_CONTEXT_SNAPSHOT_BEFORE);
        context.removeMetadata(TraceConstants.TRACE_CONTEXT_SNAPSHOT_AFTER);
        context.removeMetadata(TraceConstants.TRACE_SUMMARY);
    }
}
