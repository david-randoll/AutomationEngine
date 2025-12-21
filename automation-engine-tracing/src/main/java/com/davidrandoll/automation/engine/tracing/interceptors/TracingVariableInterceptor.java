package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.tracing.TraceChildren;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import com.davidrandoll.automation.engine.tracing.TraceSnapshot;
import com.davidrandoll.automation.engine.tracing.VariableTraceEntry;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableChain;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import lombok.extern.slf4j.Slf4j;

import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.filterTraceData;
import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.hasAnyChildren;

/**
 * Interceptor that captures trace information for variable resolution.
 */
@Slf4j
public class TracingVariableInterceptor implements IVariableInterceptor {

    @Override
    public void intercept(EventContext eventContext, VariableContext variableContext, IVariableChain chain) {
        TraceContext traceContext = TraceContext.get(eventContext);

        if (traceContext == null) {
            // Tracing not enabled, just proceed
            chain.resolve(eventContext, variableContext);
            return;
        }

        long startedAt = System.currentTimeMillis();

        // Capture before snapshot
        TraceSnapshot beforeSnapshot = captureSnapshot(eventContext, variableContext);

        // Enter nested scope for child tracing
        TraceChildren children = traceContext.enterNestedScope();

        try {
            // Execute the variable resolution
            chain.resolve(eventContext, variableContext);
        } finally {
            // Exit nested scope
            traceContext.exitNestedScope();
        }

        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, variableContext);

        // Extract type and alias from context
        String type = variableContext.getVariable();
        String alias = variableContext.getAlias();

        // Create trace entry with children if any were added
        VariableTraceEntry entry = VariableTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .children(hasAnyChildren(children) ? children : null)
                .build();

        traceContext.addVariable(entry);
        log.debug("Variable trace captured: type={}, alias={}", type, alias);
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, VariableContext variableContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(filterTraceData(eventContext.getEventData()))
                .contextSnapshot(filterTraceData(variableContext.getData()))
                .build();
    }
}
