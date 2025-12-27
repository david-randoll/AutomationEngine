package com.davidrandoll.automation.engine.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Context holder for trace data during automation execution.
 * Stored in EventContext metadata to track trace entries hierarchically.
 * <p>
 * Uses a stack-based approach to handle nested children (e.g., ifThenElse
 * actions
 * that contain nested conditions and actions).
 * </p>
 */
public class TraceContext {
    /**
     * ThreadLocal to store the TraceContext for the current thread.
     * Used by TracingAppender to record logs.
     */
    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    /**
     * Key used to store TraceContext in EventContext metadata.
     */
    public static final String TRACE_CONTEXT_KEY = "__traceContext";

    /**
     * The root execution trace.
     */
    private final ExecutionTrace executionTrace;

    /**
     * Stack of TraceChildren for handling nested operations.
     * The bottom of the stack is the root trace data (converted to TraceChildren).
     * When entering a nested scope (e.g., inside ifThenElse), a new TraceChildren
     * is pushed.
     */
    private final Deque<TraceChildren> childrenStack = new LinkedList<>();

    /**
     * Stack of log buffers for handling nested operations.
     */
    private final Deque<List<LogEntry>> logBufferStack = new LinkedList<>();

    public TraceContext(String alias) {
        this.executionTrace = ExecutionTrace.builder()
                .alias(alias)
                .startedAt(System.currentTimeMillis())
                .build();
        // Initialize with root level (wrapped as TraceChildren for uniform handling)
        this.childrenStack.push(createRootTraceChildren());
        // Start capturing logs at the trace level (for automation-level logs)
        this.startLogCapture();
    }

    /**
     * Sets the TraceContext for the current thread.
     */
    public static void setThreadContext(TraceContext context) {
        CURRENT.set(context);
    }

    /**
     * Clears the TraceContext for the current thread.
     */
    public static void clearThreadContext() {
        CURRENT.remove();
    }

    /**
     * Records a log message in the current scope.
     */
    public static void recordLog(LogEntry logEntry) {
        TraceContext context = CURRENT.get();
        if (context != null) {
            context.addLogToCurrentScope(logEntry);
        }
    }

    private void addLogToCurrentScope(LogEntry logEntry) {
        if (!logBufferStack.isEmpty()) {
            logBufferStack.peek().add(logEntry);
        }
    }

    /**
     * Starts capturing logs for a new component.
     */
    public void startLogCapture() {
        logBufferStack.push(new ArrayList<>());
    }

    /**
     * Stops capturing logs and returns the captured messages.
     */
    public List<LogEntry> stopLogCapture() {
        return logBufferStack.isEmpty() ? Collections.emptyList() : logBufferStack.pop();
    }

    /**
     * Gets or creates a TraceContext from the EventContext metadata.
     *
     * @param eventContext the event context
     * @param alias        the automation alias (used if creating new context)
     * @return the TraceContext
     */
    public static TraceContext getOrCreate(EventContext eventContext, String alias) {
        Object existing = eventContext.getMetadata(TRACE_CONTEXT_KEY);
        if (existing instanceof TraceContext traceContext) {
            return traceContext;
        }
        TraceContext traceContext = new TraceContext(alias);
        eventContext.addMetadata(TRACE_CONTEXT_KEY, traceContext);
        return traceContext;
    }

    /**
     * Gets the TraceContext from EventContext if it exists.
     *
     * @param eventContext the event context
     * @return the TraceContext or null if not present
     */
    public static TraceContext get(EventContext eventContext) {
        Object existing = eventContext.getMetadata().get(TRACE_CONTEXT_KEY);
        if (existing instanceof TraceContext traceContext)
            return traceContext;
        return null;
    }

    /**
     * Checks if tracing is enabled for this event context.
     *
     * @param eventContext the event context
     * @return true if tracing is active
     */
    public static boolean isTracingEnabled(EventContext eventContext) {
        return get(eventContext) != null;
    }

    /**
     * Adds a variable trace entry to the current scope.
     */
    public void addVariable(VariableTraceEntry entry) {
        getCurrentChildren().getVariables().add(entry);
    }

    /**
     * Adds a trigger trace entry to the current scope.
     */
    public void addTrigger(TriggerTraceEntry entry) {
        getCurrentChildren().getTriggers().add(entry);
    }

    /**
     * Adds a condition trace entry to the current scope.
     */
    public void addCondition(ConditionTraceEntry entry) {
        getCurrentChildren().getConditions().add(entry);
    }

    /**
     * Adds an action trace entry to the current scope.
     */
    public void addAction(ActionTraceEntry entry) {
        getCurrentChildren().getActions().add(entry);
    }

    /**
     * Sets the result trace entry for the current scope.
     */
    public void setResult(ResultTraceEntry entry) {
        getCurrentChildren().setResult(entry);
    }

    /**
     * Enters a new nested scope (e.g., when entering ifThenElse action).
     * Returns the TraceChildren that should be attached to the parent entry.
     */
    public TraceChildren enterNestedScope() {
        TraceChildren children = new TraceChildren();
        childrenStack.push(children);
        return children;
    }

    /**
     * Exits the current nested scope.
     */
    public void exitNestedScope() {
        if (childrenStack.size() > 1) {
            childrenStack.pop();
        }
    }

    /**
     * Gets the current scope's children container.
     */
    private TraceChildren getCurrentChildren() {
        return childrenStack.peek();
    }

    /**
     * Completes the trace and returns the final ExecutionTrace.
     */
    public ExecutionTrace complete() {
        executionTrace.setFinishedAt(System.currentTimeMillis());

        // Collect logs from two sources:
        // 1. Trace-level logs (captured during automation execution)
        List<LogEntry> traceLevelLogs = this.stopLogCapture();
        
        // 2. Component logs (from all child blocks - variables, triggers, conditions, actions, results)
        List<LogEntry> allLogs = new ArrayList<>(traceLevelLogs);
        
        // Transfer from root TraceChildren to ExecutionTrace.TraceData
        TraceChildren root = childrenStack.peekLast();
        if (root != null) {
            // Aggregate logs from all components
            collectLogsFromVariables(root.getVariables(), allLogs);
            collectLogsFromTriggers(root.getTriggers(), allLogs);
            collectLogsFromConditions(root.getConditions(), allLogs);
            collectLogsFromActions(root.getActions(), allLogs);
            if (root.getResult() != null) {
                collectLogsFromResult(root.getResult(), allLogs);
            }
            
            executionTrace.getTrace().setVariables(root.getVariables());
            executionTrace.getTrace().setTriggers(root.getTriggers());
            executionTrace.getTrace().setConditions(root.getConditions());
            executionTrace.getTrace().setActions(root.getActions());
            executionTrace.getTrace().setResult(root.getResult());
        }
        
        executionTrace.setLogs(allLogs);
        return executionTrace;
    }

    /**
     * Collects logs from variable entries, including nested children.
     */
    private void collectLogsFromVariables(List<VariableTraceEntry> variables, List<LogEntry> allLogs) {
        if (variables == null) return;
        for (VariableTraceEntry var : variables) {
            if (var.getLogs() != null) {
                allLogs.addAll(var.getLogs());
            }
            if (var.getChildren() != null) {
                collectLogsFromChildren(var.getChildren(), allLogs);
            }
        }
    }

    /**
     * Collects logs from trigger entries, including nested children.
     */
    private void collectLogsFromTriggers(List<TriggerTraceEntry> triggers, List<LogEntry> allLogs) {
        if (triggers == null) return;
        for (TriggerTraceEntry trigger : triggers) {
            if (trigger.getLogs() != null) {
                allLogs.addAll(trigger.getLogs());
            }
            if (trigger.getChildren() != null) {
                collectLogsFromChildren(trigger.getChildren(), allLogs);
            }
        }
    }

    /**
     * Collects logs from condition entries, including nested children.
     */
    private void collectLogsFromConditions(List<ConditionTraceEntry> conditions, List<LogEntry> allLogs) {
        if (conditions == null) return;
        for (ConditionTraceEntry condition : conditions) {
            if (condition.getLogs() != null) {
                allLogs.addAll(condition.getLogs());
            }
            if (condition.getChildren() != null) {
                collectLogsFromChildren(condition.getChildren(), allLogs);
            }
        }
    }

    /**
     * Collects logs from action entries, including nested children.
     */
    private void collectLogsFromActions(List<ActionTraceEntry> actions, List<LogEntry> allLogs) {
        if (actions == null) return;
        for (ActionTraceEntry action : actions) {
            if (action.getLogs() != null) {
                allLogs.addAll(action.getLogs());
            }
            if (action.getChildren() != null) {
                collectLogsFromChildren(action.getChildren(), allLogs);
            }
        }
    }

    /**
     * Collects logs from result entry, including nested children.
     */
    private void collectLogsFromResult(ResultTraceEntry result, List<LogEntry> allLogs) {
        if (result == null) return;
        if (result.getLogs() != null) {
            allLogs.addAll(result.getLogs());
        }
        if (result.getChildren() != null) {
            collectLogsFromChildren(result.getChildren(), allLogs);
        }
    }

    /**
     * Recursively collects logs from nested children.
     */
    private void collectLogsFromChildren(TraceChildren children, List<LogEntry> allLogs) {
        if (children == null) return;
        collectLogsFromVariables(children.getVariables(), allLogs);
        collectLogsFromTriggers(children.getTriggers(), allLogs);
        collectLogsFromConditions(children.getConditions(), allLogs);
        collectLogsFromActions(children.getActions(), allLogs);
        if (children.getResult() != null) {
            collectLogsFromResult(children.getResult(), allLogs);
        }
    }

    /**
     * Creates a TraceChildren wrapper for the root level trace data.
     */
    private TraceChildren createRootTraceChildren() {
        return new TraceChildren();
    }

    /**
     * Gets the execution trace (for inspection, not necessarily complete).
     */
    public ExecutionTrace getExecutionTrace() {
        return executionTrace;
    }

}
