package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionChain;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.tracing.ActionTraceEntry;
import com.davidrandoll.automation.engine.core.tracing.TraceChildren;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.core.tracing.TraceSnapshot;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor that captures trace information for action execution.
 */
@Slf4j
public class TracingActionInterceptor implements IActionInterceptor {

    /**
     * Key used to store the type in context data.
     */
    public static final String TYPE_KEY = "__type";

    /**
     * Key used to mark that this action has nested children.
     */
    public static final String HAS_CHILDREN_KEY = "__hasChildren";

    @Override
    public void intercept(EventContext eventContext, ActionContext actionContext, IActionChain chain) {
        TraceContext traceContext = TraceContext.get(eventContext);

        if (traceContext == null) {
            // Tracing not enabled, just proceed
            chain.execute(eventContext, actionContext);
            return;
        }

        long startedAt = System.currentTimeMillis();

        // Capture before snapshot
        TraceSnapshot beforeSnapshot = captureSnapshot(eventContext, actionContext);

        // Check if this action might have children (e.g., ifThenElse, forEachAction)
        // and enter nested scope for child tracing
        TraceChildren children = traceContext.enterNestedScope();

        try {
            // Execute the action
            chain.execute(eventContext, actionContext);
        } finally {
            // Exit nested scope
            traceContext.exitNestedScope();
        }

        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, actionContext);

        // Extract type and alias from context
        String type = extractType(actionContext);
        String alias = extractAlias(actionContext);

        // Create trace entry with children if any were added
        ActionTraceEntry entry = ActionTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .children(hasAnyChildren(children) ? children : null)
                .build();

        traceContext.addAction(entry);
        log.debug("Action trace captured: type={}, alias={}", type, alias);
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, ActionContext actionContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(new HashMap<>(eventContext.getEventData()))
                .contextSnapshot(filterContextData(actionContext.getData()))
                .build();
    }

    private String extractType(ActionContext context) {
        Object type = context.getData().get(TYPE_KEY);
        return type != null ? type.toString() : null;
    }

    private String extractAlias(ActionContext context) {
        Object alias = context.getData().get("alias");
        return alias != null ? alias.toString() : null;
    }

    /**
     * Filters out internal keys (starting with __) from context data for the snapshot.
     */
    private Map<String, Object> filterContextData(Map<String, Object> data) {
        if (data == null) {
            return new HashMap<>();
        }
        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!entry.getKey().startsWith("__")) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * Checks if the children container has any entries.
     */
    private boolean hasAnyChildren(TraceChildren children) {
        if (children == null) {
            return false;
        }
        return !children.getVariables().isEmpty()
                || !children.getTriggers().isEmpty()
                || !children.getConditions().isEmpty()
                || !children.getActions().isEmpty()
                || children.getResult() != null;
    }
}
