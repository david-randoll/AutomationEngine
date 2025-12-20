package com.davidrandoll.automation.engine.core.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Context holder for trace data during automation execution.
 * Stored in EventContext metadata to track trace entries hierarchically.
 * <p>
 * Uses a stack-based approach to handle nested children (e.g., ifThenElse actions
 * that contain nested conditions and actions).
 * </p>
 */
public class TraceContext {
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
     * When entering a nested scope (e.g., inside ifThenElse), a new TraceChildren is pushed.
     */
    private final Deque<TraceChildren> childrenStack = new LinkedList<>();

    public TraceContext(String alias) {
        this.executionTrace = ExecutionTrace.builder()
                .alias(alias)
                .startedAt(System.currentTimeMillis())
                .build();
        // Initialize with root level (wrapped as TraceChildren for uniform handling)
        this.childrenStack.push(createRootTraceChildren());
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
        if (existing instanceof TraceContext traceContext) return traceContext;
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
        // Transfer from root TraceChildren to ExecutionTrace.TraceData
        TraceChildren root = childrenStack.peekLast();
        if (root != null) {
            executionTrace.getTrace().setVariables(root.getVariables());
            executionTrace.getTrace().setTriggers(root.getTriggers());
            executionTrace.getTrace().setConditions(root.getConditions());
            executionTrace.getTrace().setActions(root.getActions());
            executionTrace.getTrace().setResult(root.getResult());
        }
        return executionTrace;
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
