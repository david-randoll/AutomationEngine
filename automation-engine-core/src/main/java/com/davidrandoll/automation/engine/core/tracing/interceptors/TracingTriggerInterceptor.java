package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.core.tracing.TraceSnapshot;
import com.davidrandoll.automation.engine.core.tracing.TriggerTraceEntry;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerChain;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor that captures trace information for trigger evaluation.
 */
@Slf4j
public class TracingTriggerInterceptor implements ITriggerInterceptor {

    /**
     * Key used to store the type in context data.
     */
    public static final String TYPE_KEY = "__type";

    @Override
    public boolean intercept(EventContext eventContext, TriggerContext triggerContext, ITriggerChain chain) {
        TraceContext traceContext = TraceContext.get(eventContext);

        if (traceContext == null) {
            // Tracing not enabled, just proceed
            return chain.isTriggered(eventContext, triggerContext);
        }

        long startedAt = System.currentTimeMillis();

        // Capture before snapshot
        TraceSnapshot beforeSnapshot = captureSnapshot(eventContext, triggerContext);

        // Execute the trigger evaluation
        boolean activated = chain.isTriggered(eventContext, triggerContext);

        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, triggerContext);

        // Extract type and alias from context
        String type = extractType(triggerContext);
        String alias = extractAlias(triggerContext);

        // Create trace entry
        TriggerTraceEntry entry = TriggerTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .activated(activated)
                .build();

        traceContext.addTrigger(entry);
        log.debug("Trigger trace captured: type={}, alias={}, activated={}", type, alias, activated);

        return activated;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, TriggerContext triggerContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(new HashMap<>(eventContext.getEventData()))
                .contextSnapshot(filterContextData(triggerContext.getData()))
                .build();
    }

    private String extractType(TriggerContext context) {
        Object type = context.getData().get(TYPE_KEY);
        return type != null ? type.toString() : null;
    }

    private String extractAlias(TriggerContext context) {
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
