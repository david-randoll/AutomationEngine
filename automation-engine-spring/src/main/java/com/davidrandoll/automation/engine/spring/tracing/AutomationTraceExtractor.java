package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;

import java.util.*;

/**
 * Utility for extracting trace data from AutomationResult or EventContext.
 * Provides convenient methods to access structured trace information.
 */
public final class AutomationTraceExtractor {
    
    private AutomationTraceExtractor() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Extract trace data from AutomationResult.additionalFields.
     *
     * @param result The automation result
     * @return Trace data map, or empty map if no trace present
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractTrace(AutomationResult result) {
        if (result == null || result.getAdditionalFields() == null) {
            return Collections.emptyMap();
        }
        
        Object trace = result.getAdditionalFields().get("trace");
        if (trace instanceof Map) {
            return (Map<String, Object>) trace;
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * Extract trace data directly from EventContext metadata.
     * Useful for accessing trace data before AutomationResult is created.
     *
     * @param eventContext The event context
     * @return Trace data map built from metadata
     */
    public static Map<String, Object> extractTraceFromContext(EventContext eventContext) {
        if (eventContext == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> trace = new LinkedHashMap<>();
        
        // Extract basic fields
        extractIfPresent(eventContext, TraceConstants.TRACE_ID, trace, "traceId");
        extractIfPresent(eventContext, TraceConstants.TRACE_EXECUTED, trace, "executed");
        
        // Extract timing
        Long startNanos = (Long) eventContext.getMetadata().get(TraceConstants.TRACE_AUTOMATION_START);
        Long endNanos = (Long) eventContext.getMetadata().get(TraceConstants.TRACE_AUTOMATION_END);
        Long durationNanos = (Long) eventContext.getMetadata().get(TraceConstants.TRACE_AUTOMATION_DURATION_NANOS);
        
        if (startNanos != null && durationNanos != null) {
            Map<String, Object> timing = new LinkedHashMap<>();
            timing.put("startNanos", startNanos);
            if (endNanos != null) {
                timing.put("endNanos", endNanos);
            }
            timing.put("durationNanos", durationNanos);
            timing.put("durationMillis", durationNanos / 1_000_000.0);
            trace.put("timing", timing);
        }
        
        // Extract component traces
        extractListIfPresent(eventContext, TraceConstants.TRACE_VARIABLES, trace, "variables");
        extractListIfPresent(eventContext, TraceConstants.TRACE_TRIGGERS, trace, "triggers");
        extractListIfPresent(eventContext, TraceConstants.TRACE_CONDITIONS, trace, "conditions");
        extractListIfPresent(eventContext, TraceConstants.TRACE_ACTIONS, trace, "actions");
        extractIfPresent(eventContext, TraceConstants.TRACE_RESULT, trace, "result");
        
        // Extract snapshots
        extractIfPresent(eventContext, TraceConstants.TRACE_CONTEXT_SNAPSHOT_BEFORE, trace, "contextSnapshotBefore");
        extractIfPresent(eventContext, TraceConstants.TRACE_CONTEXT_SNAPSHOT_AFTER, trace, "contextSnapshotAfter");
        
        return trace;
    }
    
    /**
     * Get the trace ID from AutomationResult.
     *
     * @param result The automation result
     * @return Trace ID, or null if not present
     */
    public static String getTraceId(AutomationResult result) {
        Map<String, Object> trace = extractTrace(result);
        Object traceId = trace.get("traceId");
        return traceId != null ? traceId.toString() : null;
    }
    
    /**
     * Get the execution duration in milliseconds from AutomationResult.
     *
     * @param result The automation result
     * @return Duration in milliseconds, or null if not present
     */
    @SuppressWarnings("unchecked")
    public static Double getDurationMillis(AutomationResult result) {
        Map<String, Object> trace = extractTrace(result);
        Object timing = trace.get("timing");
        
        if (timing instanceof Map) {
            Map<String, Object> timingMap = (Map<String, Object>) timing;
            Object durationMillis = timingMap.get("durationMillis");
            if (durationMillis instanceof Number) {
                return ((Number) durationMillis).doubleValue();
            }
        }
        
        return null;
    }
    
    /**
     * Get the list of executed actions from AutomationResult.
     *
     * @param result The automation result
     * @return List of action trace entries
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getActions(AutomationResult result) {
        Map<String, Object> trace = extractTrace(result);
        Object actions = trace.get("actions");
        
        if (actions instanceof List) {
            return (List<Map<String, Object>>) actions;
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Get the list of evaluated conditions from AutomationResult.
     *
     * @param result The automation result
     * @return List of condition trace entries
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getConditions(AutomationResult result) {
        Map<String, Object> trace = extractTrace(result);
        Object conditions = trace.get("conditions");
        
        if (conditions instanceof List) {
            return (List<Map<String, Object>>) conditions;
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Get the list of triggered events from AutomationResult.
     *
     * @param result The automation result
     * @return List of trigger trace entries
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTriggers(AutomationResult result) {
        Map<String, Object> trace = extractTrace(result);
        Object triggers = trace.get("triggers");
        
        if (triggers instanceof List) {
            return (List<Map<String, Object>>) triggers;
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Check if any actions had exceptions during execution.
     *
     * @param result The automation result
     * @return true if any action threw an exception
     */
    public static boolean hasActionExceptions(AutomationResult result) {
        Map<String, Object> trace = extractTrace(result);
        Object exceptionsCount = trace.get("actionsWithExceptions");
        
        if (exceptionsCount instanceof Number) {
            return ((Number) exceptionsCount).intValue() > 0;
        }
        
        return false;
    }
    
    private static void extractIfPresent(EventContext context, String metadataKey, 
                                        Map<String, Object> target, String targetKey) {
        Object value = context.getMetadata().get(metadataKey);
        if (value != null) {
            target.put(targetKey, value);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void extractListIfPresent(EventContext context, String metadataKey, 
                                            Map<String, Object> target, String targetKey) {
        Object value = context.getMetadata().get(metadataKey);
        if (value instanceof List && !((List<?>) value).isEmpty()) {
            target.put(targetKey, value);
        }
    }
}
