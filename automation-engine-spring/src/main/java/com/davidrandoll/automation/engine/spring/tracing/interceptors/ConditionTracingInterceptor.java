package com.davidrandoll.automation.engine.spring.tracing.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.tracing.TraceConstants;
import com.davidrandoll.automation.engine.spring.tracing.TraceDataCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Interceptor for tracing condition evaluation.
 * Captures condition type, satisfaction result, and execution duration.
 */
@Slf4j
@Order(200) // Execute after templating interceptors
public class ConditionTracingInterceptor implements IConditionInterceptor {

    @Override
    public boolean intercept(EventContext eventContext, ConditionContext conditionContext, IConditionChain chain) {
        // Only trace if tracing is enabled
        if (!TraceDataCollector.isTracingEnabled(eventContext)) {
            return chain.isSatisfied(eventContext, conditionContext);
        }

        long startNanos = System.nanoTime();

        // Execute condition evaluation
        boolean satisfied = chain.isSatisfied(eventContext, conditionContext);

        long endNanos = System.nanoTime();

        // Build trace entry
        Map<String, Object> traceEntry = TraceDataCollector.createTimingEntry(startNanos, endNanos);
        String alias = conditionContext.getData().get("alias") != null
                ? conditionContext.getData().get("alias").toString()
                : "unknown";
        traceEntry.put(TraceConstants.FIELD_ALIAS, alias);
        traceEntry.put(TraceConstants.FIELD_TYPE, conditionContext.getClass().getSimpleName());
        traceEntry.put(TraceConstants.FIELD_SATISFIED, satisfied);

        // Store in trace list
        TraceDataCollector.appendToTraceList(eventContext, TraceConstants.TRACE_CONDITIONS, traceEntry);

        log.trace("Condition evaluated: {} = {} in {}ns",
                alias, satisfied, traceEntry.get(TraceConstants.FIELD_DURATION_NANOS));

        return satisfied;
    }
}
