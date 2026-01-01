package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionChain;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.tracing.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.filterTraceData;
import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.hasAnyChildren;

/**
 * Interceptor that captures trace information for action execution.
 */
@Slf4j
public class TracingActionInterceptor implements IActionInterceptor {

    /**
     * Key used to mark that this action has nested children.
     */
    public static final String HAS_CHILDREN_KEY = "__hasChildren";

    @Override
    public ActionResult intercept(EventContext eventContext, ActionContext actionContext, IActionChain chain) {
        TraceContext traceContext = TraceContext.get(eventContext);

        if (traceContext == null) {
            // Tracing not enabled, just proceed
            return chain.execute(eventContext, actionContext);
        }

        long startedAt = System.currentTimeMillis();

        // Capture before snapshot
        TraceSnapshot beforeSnapshot = captureSnapshot(eventContext, actionContext);

        // Check if this action might have children (e.g., ifThenElse, forEachAction)
        // and enter nested scope for child tracing
        TraceChildren children = traceContext.enterNestedScope();
        traceContext.startLogCapture();

        ActionResult result;
        try {
            // Execute the action
            result = chain.execute(eventContext, actionContext);
        } finally {
            // Exit nested scope
            traceContext.exitNestedScope();
        }

        List<LogEntry> logs = traceContext.stopLogCapture();
        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, actionContext);

        // Extract type and alias from context
        String type = actionContext.getAction();
        String alias = actionContext.getAlias();

        // Create trace entry with children if any were added
        ActionTraceEntry entry = ActionTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .children(hasAnyChildren(children) ? children : null)
                .logs(logs)
                .build();

        traceContext.addAction(entry);
        log.debug("Action trace captured: type={}, alias={}", type, alias);
        return result;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, ActionContext actionContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(filterTraceData(eventContext.getEventData()))
                .contextSnapshot(filterTraceData(actionContext.getData()))
                .build();
    }
}
