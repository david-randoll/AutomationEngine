package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultChain;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultInterceptor;
import com.davidrandoll.automation.engine.tracing.LogEntry;
import com.davidrandoll.automation.engine.tracing.ResultTraceEntry;
import com.davidrandoll.automation.engine.tracing.TraceChildren;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import com.davidrandoll.automation.engine.tracing.TraceSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.filterTraceData;
import static com.davidrandoll.automation.engine.tracing.utils.TraceUtils.hasAnyChildren;

/**
 * Interceptor that captures trace information for result computation.
 */
@Slf4j
@RequiredArgsConstructor
public class TracingResultInterceptor implements IResultInterceptor {
    private final ObjectMapper objectMapper;

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

        // Enter nested scope for child tracing
        TraceChildren children = traceContext.enterNestedScope();
        traceContext.startLogCapture();

        Object result;
        try {
            // Execute the result computation
            result = chain.getExecutionSummary(eventContext, resultContext);
        } finally {
            // Exit nested scope
            traceContext.exitNestedScope();
        }

        List<LogEntry> logs = traceContext.stopLogCapture();
        long finishedAt = System.currentTimeMillis();

        // Capture after snapshot
        TraceSnapshot afterSnapshot = captureSnapshot(eventContext, resultContext);

        // Create trace entry with children if any were added
        ResultTraceEntry entry = ResultTraceEntry.builder()
                .type(resultContext.getResult())
                .alias(resultContext.getAlias())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .before(beforeSnapshot)
                .after(afterSnapshot)
                .result(result)
                .children(hasAnyChildren(children) ? children : null)
                .logs(logs)
                .build();

        traceContext.setResult(entry);
        log.debug("Result trace captured");

        return result;
    }

    private TraceSnapshot captureSnapshot(EventContext eventContext, ResultContext resultContext) {
        return TraceSnapshot.builder()
                .eventSnapshot(filterTraceData(eventContext.getEventData()))
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
