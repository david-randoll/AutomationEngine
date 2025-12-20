package com.davidrandoll.automation.engine.spring.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.interceptors.IActionChain;
import com.davidrandoll.automation.engine.spring.spi.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.spring.spi.modules.actions.ActionContext;
import com.davidrandoll.automation.engine.spring.tracing.TraceConstants;
import com.davidrandoll.automation.engine.spring.tracing.TraceDataCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Interceptor for tracing action execution.
 * Captures action type, execution duration, and any exceptions thrown.
 */
@Slf4j
@Order(200) // Execute after templating interceptors
public class ActionTracingInterceptor implements IActionInterceptor {
    
    @Override
    public void intercept(EventContext eventContext, ActionContext actionContext, IActionChain chain) {
        // Only trace if tracing is enabled
        if (!TraceDataCollector.isTracingEnabled(eventContext)) {
            chain.execute(eventContext, actionContext);
            return;
        }
        
        long startNanos = System.nanoTime();
        String alias = actionContext.getAlias();
        Throwable exception = null;
        
        try {
            // Execute action
            chain.execute(eventContext, actionContext);
        } catch (Exception e) {
            exception = e;
            throw e; // Re-throw after capturing
        } finally {
            long endNanos = System.nanoTime();
            
            // Build trace entry
            Map<String, Object> traceEntry = TraceDataCollector.createTimingEntry(startNanos, endNanos);
            traceEntry.put(TraceConstants.FIELD_ALIAS, alias);
            traceEntry.put(TraceConstants.FIELD_TYPE, actionContext.getClass().getSimpleName());
            
            if (exception != null) {
                traceEntry.put(TraceConstants.FIELD_EXCEPTION, exception.getClass().getName());
                traceEntry.put(TraceConstants.FIELD_EXCEPTION_MESSAGE, exception.getMessage());
            }
            
            // Store in trace list
            TraceDataCollector.appendToTraceList(eventContext, TraceConstants.TRACE_ACTIONS, traceEntry);
            
            if (exception != null) {
                log.trace("Action executed with exception: {} in {}ns - {}", 
                          alias, traceEntry.get(TraceConstants.FIELD_DURATION_NANOS), exception.getMessage());
            } else {
                log.trace("Action executed: {} in {}ns", 
                          alias, traceEntry.get(TraceConstants.FIELD_DURATION_NANOS));
            }
        }
    }
}
