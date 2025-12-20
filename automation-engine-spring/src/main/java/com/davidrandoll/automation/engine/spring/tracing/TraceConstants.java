package com.davidrandoll.automation.engine.spring.tracing;

/**
 * Constants for tracing metadata keys in EventContext.
 * All trace-related metadata uses the __trace_ prefix to avoid collision with user-defined variables.
 */
public final class TraceConstants {
    
    private TraceConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Top-level trace keys
    public static final String TRACE_ID = "__trace_id";
    public static final String TRACE_AUTOMATION_START = "__trace_automation_start";
    public static final String TRACE_AUTOMATION_END = "__trace_automation_end";
    public static final String TRACE_AUTOMATION_DURATION_NANOS = "__trace_automation_duration_nanos";
    public static final String TRACE_EXECUTED = "__trace_executed";
    
    // Component trace lists
    public static final String TRACE_VARIABLES = "__trace_variables";
    public static final String TRACE_TRIGGERS = "__trace_triggers";
    public static final String TRACE_CONDITIONS = "__trace_conditions";
    public static final String TRACE_ACTIONS = "__trace_actions";
    public static final String TRACE_RESULT = "__trace_result";
    
    // Context snapshot keys
    public static final String TRACE_CONTEXT_SNAPSHOT_BEFORE = "__trace_context_snapshot_before";
    public static final String TRACE_CONTEXT_SNAPSHOT_AFTER = "__trace_context_snapshot_after";
    
    // Summary key
    public static final String TRACE_SUMMARY = "__trace_summary";
    
    // Common field names in trace entries
    public static final String FIELD_ALIAS = "alias";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_DURATION_NANOS = "durationNanos";
    public static final String FIELD_START_TIME_NANOS = "startTimeNanos";
    public static final String FIELD_END_TIME_NANOS = "endTimeNanos";
    public static final String FIELD_ACTIVATED = "activated";
    public static final String FIELD_SATISFIED = "satisfied";
    public static final String FIELD_EXCEPTION = "exception";
    public static final String FIELD_EXCEPTION_MESSAGE = "exceptionMessage";
    public static final String FIELD_BEFORE_VALUE = "beforeValue";
    public static final String FIELD_AFTER_VALUE = "afterValue";
    public static final String FIELD_RESULT_TYPE = "resultType";
    public static final String FIELD_RESULT_VALUE = "resultValue";
}
