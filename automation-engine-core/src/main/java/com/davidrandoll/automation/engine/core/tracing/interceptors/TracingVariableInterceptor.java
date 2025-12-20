package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.tracing.TraceChildren;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.core.tracing.TraceSnapshot;
import com.davidrandoll.automation.engine.core.tracing.VariableTraceEntry;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableChain;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.davidrandoll.automation.engine.core.tracing.utils.TraceUtils.hasAnyChildren;

/**
 * Interceptor that captures trace information for variable resolution.
 */
@Slf4j
public class TracingVariableInterceptor implements IVariableInterceptor {

    /**
     * Key used to store the type in context data.
     */
    public static final String TYPE_KEY = "__type";

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
        String type = extractType(variableContext);
        String alias = extractAlias(variableContext);

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

    private String extractType(VariableContext context) {
        Object type = context.getData().get(TYPE_KEY);
        return type != null ? type.toString() : null;
    }

    private String extractAlias(VariableContext context) {
        Object alias = context.getData().get("alias");
        return alias != null ? alias.toString() : null;
    }

    /**
     * Filters out internal keys (starting with __) from context data for the snapshot.
     */
    private Map<String, Object> filterTraceData(Map<String, Object> data) {
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
}
