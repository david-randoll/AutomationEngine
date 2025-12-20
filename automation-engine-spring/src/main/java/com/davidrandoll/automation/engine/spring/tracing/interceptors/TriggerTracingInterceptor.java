package com.davidrandoll.automation.engine.spring.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.interceptors.ITriggerChain;
import com.davidrandoll.automation.engine.spring.spi.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.spring.spi.modules.triggers.TriggerContext;
import com.davidrandoll.automation.engine.spring.tracing.TraceConstants;
import com.davidrandoll.automation.engine.spring.tracing.TraceDataCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Interceptor for tracing trigger evaluation.
 * Captures trigger type, activation result, and execution duration.
 */
@Slf4j
@Order(200) // Execute after templating interceptors
public class TriggerTracingInterceptor implements ITriggerInterceptor {
    
    @Override
    public boolean intercept(EventContext eventContext, TriggerContext triggerContext, ITriggerChain chain) {
        // Only trace if tracing is enabled
        if (!TraceDataCollector.isTracingEnabled(eventContext)) {
            return chain.isTriggered(eventContext, triggerContext);
        }
        
        long startNanos = System.nanoTime();
        
        // Execute trigger evaluation
        boolean activated = chain.isTriggered(eventContext, triggerContext);
        
        long endNanos = System.nanoTime();
        
        // Build trace entry
        Map<String, Object> traceEntry = TraceDataCollector.createTimingEntry(startNanos, endNanos);
        traceEntry.put(TraceConstants.FIELD_ALIAS, triggerContext.getAlias());
        traceEntry.put(TraceConstants.FIELD_TYPE, triggerContext.getClass().getSimpleName());
        traceEntry.put(TraceConstants.FIELD_ACTIVATED, activated);
        
        // Store in trace list
        TraceDataCollector.appendToTraceList(eventContext, TraceConstants.TRACE_TRIGGERS, traceEntry);
        
        log.trace("Trigger evaluated: {} = {} in {}ns", 
                  triggerContext.getAlias(), activated, traceEntry.get(TraceConstants.FIELD_DURATION_NANOS));
        
        return activated;
    }
}
