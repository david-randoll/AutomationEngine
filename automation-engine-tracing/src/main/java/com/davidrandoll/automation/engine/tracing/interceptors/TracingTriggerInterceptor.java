package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerChain;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.tracing.TraceChildren;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import com.davidrandoll.automation.engine.tracing.TraceSnapshot;
import com.davidrandoll.automation.engine.tracing.TriggerTraceEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.filterTraceData;
import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.hasAnyChildren;

/**
 * Interceptor that captures trace information for trigger evaluation.
 */
@Slf4j
public class TracingTriggerInterceptor implements ITriggerInterceptor {

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

        // Enter nested scope for child tracing
        TraceChildren children = traceContext.enterNestedScope();
        traceContext.startLogCapture();

        boolean activated;
        try {
            // Execute the trigger evaluation
            activated = chain.isTriggered(eventContext, triggerContext);
        } finally {
            // Exit nested scope
            traceContext.exitNestedScope();
        }

        List<String> logs = traceContext.stopLogCapture();
        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, triggerContext);

        // Extract type and alias from context
        String type = triggerContext.getTrigger();
        String alias = triggerContext.getAlias();

        // Create trace entry with children if any were added
        TriggerTraceEntry entry = TriggerTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .activated(activated)
                .children(hasAnyChildren(children) ? children : null)
                .logs(logs)
                .build();

        traceContext.addTrigger(entry);
        log.debug("Trigger trace captured: type={}, alias={}, activated={}", type, alias, activated);

        return activated;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, TriggerContext triggerContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(filterTraceData(eventContext.getEventData()))
                .contextSnapshot(filterTraceData(triggerContext.getData()))
                .build();
    }
}
