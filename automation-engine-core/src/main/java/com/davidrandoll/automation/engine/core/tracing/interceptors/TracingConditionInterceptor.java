package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.tracing.ConditionTraceEntry;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.core.tracing.TraceSnapshot;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor that captures trace information for condition evaluation.
 */
@Slf4j
public class TracingConditionInterceptor implements IConditionInterceptor {

    /**
     * Key used to store the type in context data.
     */
    public static final String TYPE_KEY = "__type";

    @Override
    public boolean intercept(EventContext eventContext, ConditionContext conditionContext, IConditionChain chain) {
        TraceContext traceContext = TraceContext.get(eventContext);

        if (traceContext == null) {
            // Tracing not enabled, just proceed
            return chain.isSatisfied(eventContext, conditionContext);
        }

        long startedAt = System.currentTimeMillis();

        // Capture before snapshot
        TraceSnapshot beforeSnapshot = captureSnapshot(eventContext, conditionContext);

        // Execute the condition evaluation
        boolean satisfied = chain.isSatisfied(eventContext, conditionContext);

        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, conditionContext);

        // Extract type and alias from context
        String type = extractType(conditionContext);
        String alias = extractAlias(conditionContext);

        // Create trace entry
        ConditionTraceEntry entry = ConditionTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .satisfied(satisfied)
                .build();

        traceContext.addCondition(entry);
        log.debug("Condition trace captured: type={}, alias={}, satisfied={}", type, alias, satisfied);

        return satisfied;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, ConditionContext conditionContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(new HashMap<>(eventContext.getEventData()))
                .contextSnapshot(filterContextData(conditionContext.getData()))
                .build();
    }

    private String extractType(ConditionContext context) {
        Object type = context.getData().get(TYPE_KEY);
        return type != null ? type.toString() : null;
    }

    private String extractAlias(ConditionContext context) {
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
}
