package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultChain;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.core.tracing.ResultTraceEntry;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.core.tracing.TraceSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor that captures trace information for result computation.
 */
@Slf4j
public class TracingResultInterceptor implements IResultInterceptor {

    private final ObjectMapper objectMapper;

    public TracingResultInterceptor() {
        this.objectMapper = new ObjectMapper();
    }

    public TracingResultInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object intercept(EventContext eventContext, ResultContext resultContext, IResultChain chain) {
        TraceContext traceContext = TraceContext.get(eventContext);

        if (traceContext == null) {
            // Tracing not enabled, just proceed
            return chain.getExecutionSummary(eventContext, resultContext);
        }

        long startedAt = System.currentTimeMillis();

        // Capture before snapshot
        TraceSnapshot beforeSnapshot = captureSnapshot(eventContext, resultContext);

        // Execute the result computation
        Object result = chain.getExecutionSummary(eventContext, resultContext);

        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, resultContext);

        // Create trace entry
        ResultTraceEntry entry = ResultTraceEntry.builder()
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .result(result)
                .build();

        traceContext.setResult(entry);
        log.debug("Result trace captured");

        return result;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, ResultContext resultContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(new HashMap<>(eventContext.getEventData()))
                .contextSnapshot(convertJsonNodeToMap(resultContext.getData()))
                .build();
    }

    /**
     * Converts JsonNode to Map for the context snapshot.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertJsonNodeToMap(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.convertValue(jsonNode, Map.class);
        } catch (Exception e) {
            log.warn("Failed to convert JsonNode to Map: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
