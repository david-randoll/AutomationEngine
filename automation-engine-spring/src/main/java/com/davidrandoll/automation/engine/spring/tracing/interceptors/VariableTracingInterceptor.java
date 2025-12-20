package com.davidrandoll.automation.engine.spring.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.interceptors.IVariableChain;
import com.davidrandoll.automation.engine.spring.spi.interceptors.IVariableInterceptor;
import com.davidrandoll.automation.engine.spring.spi.modules.variables.VariableContext;
import com.davidrandoll.automation.engine.spring.tracing.TraceConstants;
import com.davidrandoll.automation.engine.spring.tracing.TraceDataCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for tracing variable resolution.
 * Captures variable alias, before/after values, and execution duration.
 */
@Slf4j
@Order(200) // Execute after templating interceptors
public class VariableTracingInterceptor implements IVariableInterceptor {
    
    @Override
    public void intercept(EventContext eventContext, VariableContext variableContext, IVariableChain chain) {
        // Only trace if tracing is enabled
        if (!TraceDataCollector.isTracingEnabled(eventContext)) {
            chain.resolve(eventContext, variableContext);
            return;
        }
        
        long startNanos = System.nanoTime();
        String alias = variableContext.getAlias();
        
        // Capture before value
        Object beforeValue = eventContext.getMetadata().get(alias);
        
        // Execute variable resolution
        chain.resolve(eventContext, variableContext);
        
        // Capture after value
        Object afterValue = eventContext.getMetadata().get(alias);
        long endNanos = System.nanoTime();
        
        // Build trace entry
        Map<String, Object> traceEntry = TraceDataCollector.createTimingEntry(startNanos, endNanos);
        traceEntry.put(TraceConstants.FIELD_ALIAS, alias);
        traceEntry.put(TraceConstants.FIELD_TYPE, variableContext.getClass().getSimpleName());
        traceEntry.put(TraceConstants.FIELD_BEFORE_VALUE, serializeValue(beforeValue));
        traceEntry.put(TraceConstants.FIELD_AFTER_VALUE, serializeValue(afterValue));
        
        // Store in trace list
        TraceDataCollector.appendToTraceList(eventContext, TraceConstants.TRACE_VARIABLES, traceEntry);
        
        log.trace("Variable resolved: {} in {}ns", alias, traceEntry.get(TraceConstants.FIELD_DURATION_NANOS));
    }
    
    private Object serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return value.getClass().getSimpleName();
    }
}
