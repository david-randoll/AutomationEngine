package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultChain;
import com.davidrandoll.automation.engine.core.tracing.ResultTraceEntry;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingResultInterceptorTest {

    @Mock
    private IResultChain chain;

    private ObjectMapper objectMapper;
    private TracingResultInterceptor interceptor;
    private EventContext eventContext;
    private ResultContext resultContext;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interceptor = new TracingResultInterceptor(objectMapper);
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);

        ObjectNode resultData = objectMapper.createObjectNode();
        resultData.put("summary", "Automation completed");
        resultContext = new ResultContext(null, null, "basic", resultData);
    }

    @Test
    void testIntercept_proceedsWhenTracingNotEnabled() {
        when(chain.getExecutionSummary(eventContext, resultContext)).thenReturn("result");

        Object result = interceptor.intercept(eventContext, resultContext, chain);

        verify(chain).getExecutionSummary(eventContext, resultContext);
        assertThat(result).isEqualTo("result");
    }

    @Test
    void testIntercept_capturesResultTraceEntry() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.getExecutionSummary(any(), any())).thenReturn("computed result");

        Object result = interceptor.intercept(eventContext, resultContext, chain);

        assertThat(result).isEqualTo("computed result");

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();

        ResultTraceEntry entry = trace.getTrace().getResult();
        assertThat(entry).isNotNull();
        assertThat(entry.getResult()).isEqualTo("computed result");
    }

    @Test
    void testIntercept_capturesBeforeAndAfterSnapshots() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.getExecutionSummary(any(), any())).thenReturn("result");

        interceptor.intercept(eventContext, resultContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ResultTraceEntry entry = trace.getTrace().getResult();

        assertThat(entry.getBefore()).isNotNull();
        assertThat(entry.getAfter()).isNotNull();
        assertThat(entry.getBefore().getEventSnapshot()).containsKey("eventType");
        assertThat(entry.getBefore().getContextSnapshot()).containsKey("summary");
    }

    @Test
    void testIntercept_recordsTimingInformation() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.getExecutionSummary(any(), any())).thenReturn("result");

        interceptor.intercept(eventContext, resultContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ResultTraceEntry entry = trace.getTrace().getResult();

        assertThat(entry.getStartedAt()).isGreaterThan(0);
        assertThat(entry.getFinishedAt()).isGreaterThanOrEqualTo(entry.getStartedAt());
    }

    @Test
    void testIntercept_handlesNullJsonNode() {
        ResultContext nullContext = new ResultContext();
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.getExecutionSummary(any(), any())).thenReturn("result");

        interceptor.intercept(eventContext, nullContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ResultTraceEntry entry = trace.getTrace().getResult();

        assertThat(entry.getBefore().getContextSnapshot()).isEmpty();
    }
}
