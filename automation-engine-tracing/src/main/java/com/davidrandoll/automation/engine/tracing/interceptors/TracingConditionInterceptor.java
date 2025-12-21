package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.tracing.ConditionTraceEntry;
import com.davidrandoll.automation.engine.tracing.TraceChildren;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import com.davidrandoll.automation.engine.tracing.TraceSnapshot;
import lombok.extern.slf4j.Slf4j;

import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.filterTraceData;
import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.hasAnyChildren;

/**
 * Interceptor that captures trace information for condition evaluation.
 */
@Slf4j
public class TracingConditionInterceptor implements IConditionInterceptor {

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

        // Enter nested scope for child tracing
        TraceChildren children = traceContext.enterNestedScope();

        boolean satisfied;
        try {
            // Execute the condition evaluation
            satisfied = chain.isSatisfied(eventContext, conditionContext);
        } finally {
            // Exit nested scope
            traceContext.exitNestedScope();
        }

        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, conditionContext);

        // Extract type and alias from context
        String type = conditionContext.getCondition();
        String alias = conditionContext.getAlias();

        // Create trace entry with children if any were added
        ConditionTraceEntry entry = ConditionTraceEntry.builder()
                .type(type)
                .alias(alias)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .satisfied(satisfied)
                .children(hasAnyChildren(children) ? children : null)
                .build();

        traceContext.addCondition(entry);
        log.debug("Condition trace captured: type={}, alias={}, satisfied={}", type, alias, satisfied);

        return satisfied;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, ConditionContext conditionContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(filterTraceData(eventContext.getEventData()))
                .contextSnapshot(filterTraceData(conditionContext.getData()))
                .build();
    }
}
