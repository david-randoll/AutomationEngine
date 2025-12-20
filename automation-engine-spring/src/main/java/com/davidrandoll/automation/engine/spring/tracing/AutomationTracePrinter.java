package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.core.result.AutomationResult;

import java.util.List;
import java.util.Map;

/**
 * Utility for pretty-printing automation execution traces.
 * Formats trace data for human-readable output, useful for debugging.
 */
public final class AutomationTracePrinter {
    
    private AutomationTracePrinter() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Pretty-print a trace from AutomationResult.
     *
     * @param result The automation result
     * @return Formatted trace string
     */
    public static String prettyPrint(AutomationResult result) {
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        return prettyPrint(trace);
    }
    
    /**
     * Pretty-print a trace map.
     *
     * @param trace The trace data map
     * @return Formatted trace string
     */
    public static String prettyPrint(Map<String, Object> trace) {
        if (trace == null || trace.isEmpty()) {
            return "No trace data available";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════════\n");
        sb.append("║ AUTOMATION EXECUTION TRACE\n");
        sb.append("╠═══════════════════════════════════════════════════════════════\n");
        
        // Basic info
        appendField(sb, "Trace ID", trace.get("traceId"));
        appendField(sb, "Automation", trace.get("automationAlias"));
        appendField(sb, "Executed", trace.get("executed"));
        
        // Timing
        @SuppressWarnings("unchecked")
        Map<String, Object> timing = (Map<String, Object>) trace.get("timing");
        if (timing != null) {
            sb.append("║\n");
            sb.append("║ ⏱ TIMING\n");
            appendField(sb, "  Duration", String.format("%.3f ms", timing.get("durationMillis")));
            appendField(sb, "  Duration (nanos)", timing.get("durationNanos"));
        }
        
        // Variables
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> variables = (List<Map<String, Object>>) trace.get("variables");
        if (variables != null && !variables.isEmpty()) {
            sb.append("║\n");
            sb.append("║ 📝 VARIABLES (").append(variables.size()).append(")\n");
            for (int i = 0; i < variables.size(); i++) {
                Map<String, Object> var = variables.get(i);
                sb.append("║   ").append(i + 1).append(". ").append(var.get("alias")).append("\n");
                appendNestedField(sb, "Before", var.get("beforeValue"));
                appendNestedField(sb, "After", var.get("afterValue"));
                appendNestedField(sb, "Duration", formatNanos(var.get("durationNanos")));
            }
        }
        
        // Triggers
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> triggers = (List<Map<String, Object>>) trace.get("triggers");
        if (triggers != null && !triggers.isEmpty()) {
            sb.append("║\n");
            sb.append("║ 🎯 TRIGGERS (").append(triggers.size()).append(")\n");
            Long activatedCount = (Long) trace.get("triggersActivated");
            if (activatedCount != null) {
                sb.append("║   Activated: ").append(activatedCount).append(" of ").append(triggers.size()).append("\n");
            }
            for (int i = 0; i < triggers.size(); i++) {
                Map<String, Object> trigger = triggers.get(i);
                boolean activated = Boolean.TRUE.equals(trigger.get("activated"));
                String icon = activated ? "✓" : "✗";
                sb.append("║   ").append(icon).append(" ").append(i + 1).append(". ")
                  .append(trigger.get("alias")).append(" (").append(trigger.get("type")).append(")\n");
                appendNestedField(sb, "Activated", activated);
                appendNestedField(sb, "Duration", formatNanos(trigger.get("durationNanos")));
            }
        }
        
        // Conditions
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) trace.get("conditions");
        if (conditions != null && !conditions.isEmpty()) {
            sb.append("║\n");
            sb.append("║ ✅ CONDITIONS (").append(conditions.size()).append(")\n");
            Boolean allSatisfied = (Boolean) trace.get("allConditionsSatisfied");
            if (allSatisfied != null) {
                sb.append("║   All Satisfied: ").append(allSatisfied ? "Yes" : "No").append("\n");
            }
            for (int i = 0; i < conditions.size(); i++) {
                Map<String, Object> condition = conditions.get(i);
                boolean satisfied = Boolean.TRUE.equals(condition.get("satisfied"));
                String icon = satisfied ? "✓" : "✗";
                sb.append("║   ").append(icon).append(" ").append(i + 1).append(". ")
                  .append(condition.get("alias")).append(" (").append(condition.get("type")).append(")\n");
                appendNestedField(sb, "Satisfied", satisfied);
                appendNestedField(sb, "Duration", formatNanos(condition.get("durationNanos")));
            }
        }
        
        // Actions
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) trace.get("actions");
        if (actions != null && !actions.isEmpty()) {
            sb.append("║\n");
            sb.append("║ ⚡ ACTIONS (").append(actions.size()).append(")\n");
            Long exceptionsCount = (Long) trace.get("actionsWithExceptions");
            if (exceptionsCount != null && exceptionsCount > 0) {
                sb.append("║   Exceptions: ").append(exceptionsCount).append("\n");
            }
            for (int i = 0; i < actions.size(); i++) {
                Map<String, Object> action = actions.get(i);
                boolean hasException = action.containsKey("exception");
                String icon = hasException ? "⚠" : "✓";
                sb.append("║   ").append(icon).append(" ").append(i + 1).append(". ")
                  .append(action.get("alias")).append(" (").append(action.get("type")).append(")\n");
                appendNestedField(sb, "Duration", formatNanos(action.get("durationNanos")));
                if (hasException) {
                    appendNestedField(sb, "Exception", action.get("exception"));
                    appendNestedField(sb, "Message", action.get("exceptionMessage"));
                }
            }
        }
        
        // Result
        @SuppressWarnings("unchecked")
        Map<String, Object> resultTrace = (Map<String, Object>) trace.get("result");
        if (resultTrace != null) {
            sb.append("║\n");
            sb.append("║ 📊 RESULT\n");
            appendNestedField(sb, "Type", resultTrace.get("resultType"));
            appendNestedField(sb, "Value", resultTrace.get("resultValue"));
            appendNestedField(sb, "Duration", formatNanos(resultTrace.get("durationNanos")));
        }
        
        // Context snapshots
        if (trace.containsKey("contextSnapshotBefore") || trace.containsKey("contextSnapshotAfter")) {
            sb.append("║\n");
            sb.append("║ 📸 CONTEXT SNAPSHOTS\n");
            if (trace.containsKey("contextSnapshotBefore")) {
                sb.append("║   Before: Available\n");
            }
            if (trace.containsKey("contextSnapshotAfter")) {
                sb.append("║   After: Available\n");
            }
        }
        
        sb.append("╚═══════════════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }
    
    /**
     * Create a compact single-line summary of a trace.
     *
     * @param result The automation result
     * @return One-line trace summary
     */
    public static String summary(AutomationResult result) {
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        if (trace.isEmpty()) {
            return "No trace data";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[Trace ").append(trace.get("traceId")).append("] ");
        sb.append(trace.get("automationAlias"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> timing = (Map<String, Object>) trace.get("timing");
        if (timing != null) {
            sb.append(String.format(" (%.3fms)", timing.get("durationMillis")));
        }
        
        sb.append(" - Executed: ").append(trace.get("executed"));
        
        return sb.toString();
    }
    
    private static void appendField(StringBuilder sb, String label, Object value) {
        sb.append("║ ").append(label).append(": ").append(value).append("\n");
    }
    
    private static void appendNestedField(StringBuilder sb, String label, Object value) {
        sb.append("║      - ").append(label).append(": ").append(value).append("\n");
    }
    
    private static String formatNanos(Object nanos) {
        if (nanos instanceof Number) {
            long nanosLong = ((Number) nanos).longValue();
            if (nanosLong < 1_000) {
                return nanosLong + " ns";
            } else if (nanosLong < 1_000_000) {
                return String.format("%.2f µs", nanosLong / 1_000.0);
            } else {
                return String.format("%.3f ms", nanosLong / 1_000_000.0);
            }
        }
        return String.valueOf(nanos);
    }
}
