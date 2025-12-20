package com.davidrandoll.automation.engine.spring.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.interceptors.IResultChain;
import com.davidrandoll.automation.engine.spring.spi.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.spring.spi.modules.results.ResultContext;
import com.davidrandoll.automation.engine.spring.tracing.TraceConstants;
import com.davidrandoll.automation.engine.spring.tracing.TraceDataCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for tracing result computation.
 * Captures result type, value summary, and computation duration.
 */
@Slf4j
@Order(200) // Execute after templating interceptors
public class ResultTracingInterceptor implements IResultInterceptor {
    
    @Override
    public Object intercept(EventContext eventContext, ResultContext resultContext, IResultChain chain) {
        // Only trace if tracing is enabled
        if (!TraceDataCollector.isTracingEnabled(eventContext)) {
            return chain.getExecutionSummary(eventContext, resultContext);
        }
        
        long startNanos = System.nanoTime();
        
        // Execute result computation
        Object result = chain.getExecutionSummary(eventContext, resultContext);
        
        long endNanos = System.nanoTime();
        
        // Build trace entry
        Map<String, Object> traceEntry = new HashMap<>();
        traceEntry.put(TraceConstants.FIELD_START_TIME_NANOS, startNanos);
        traceEntry.put(TraceConstants.FIELD_END_TIME_NANOS, endNanos);
        traceEntry.put(TraceConstants.FIELD_DURATION_NANOS, endNanos - startNanos);
        traceEntry.put(TraceConstants.FIELD_RESULT_TYPE, result != null ? result.getClass().getName() : "null");
        traceEntry.put(TraceConstants.FIELD_RESULT_VALUE, serializeResultValue(result));
        
        // Store directly in metadata (not a list, as there's only one result)
        eventContext.addMetadata(TraceConstants.TRACE_RESULT, traceEntry);
        
        log.trace("Result computed in {}ns: {}", 
                  traceEntry.get(TraceConstants.FIELD_DURATION_NANOS), 
                  traceEntry.get(TraceConstants.FIELD_RESULT_TYPE));
        
        return result;
    }
    
    private Object serializeResultValue(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof String || result instanceof Number || result instanceof Boolean) {
            return result;
        }
        if (result instanceof Map) {
            return ((Map<?, ?>) result).size() + " entries";
        }
        return result.getClass().getSimpleName();
    }
}
