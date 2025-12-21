package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableChain;
import com.davidrandoll.automation.engine.tracing.TestEvent;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import com.davidrandoll.automation.engine.tracing.TraceSnapshot;
import com.davidrandoll.automation.engine.tracing.VariableTraceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TracingVariableInterceptorTest {

    @Mock
    private IVariableChain chain;

    private TracingVariableInterceptor interceptor;
    private EventContext eventContext;
    private VariableContext variableContext;

    @BeforeEach
    void setUp() {
        interceptor = new TracingVariableInterceptor();
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);

        Map<String, Object> data = new HashMap<>();
        data.put("alias", "myVariable");
        data.put("value", "testValue");
        variableContext = new VariableContext("myVariable", null, "basic", data);
    }

    @Test
    void testIntercept_proceedsWhenTracingNotEnabled() {
        // Don't initialize trace context

        interceptor.intercept(eventContext, variableContext, chain);

        verify(chain).resolve(eventContext, variableContext);
    }

    @Test
    void testIntercept_capturesTraceEntry() {
        // Initialize tracing
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, variableContext, chain);

        verify(chain).resolve(eventContext, variableContext);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();

        assertThat(trace.getTrace().getVariables()).hasSize(1);
        VariableTraceEntry entry = trace.getTrace().getVariables().get(0);
        assertThat(entry.getType()).isEqualTo("basic");
        assertThat(entry.getAlias()).isEqualTo("myVariable");
        assertThat(entry.getStartedAt()).isGreaterThan(0);
        assertThat(entry.getFinishedAt()).isGreaterThanOrEqualTo(entry.getStartedAt());
    }

    @Test
    void testIntercept_capturesBeforeSnapshot() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, variableContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        VariableTraceEntry entry = trace.getTrace().getVariables().get(0);

        TraceSnapshot before = entry.getBefore();
        assertThat(before).isNotNull();
        assertThat(before.getEventSnapshot()).containsKey("eventType");
        assertThat(before.getContextSnapshot()).containsKey("alias");
        assertThat(before.getContextSnapshot()).containsKey("value");
        // Internal keys should be filtered out
        assertThat(before.getContextSnapshot()).doesNotContainKey("__type");
    }

    @Test
    void testIntercept_capturesAfterSnapshot() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        // Simulate chain modifying event metadata
        doAnswer(invocation -> {
            eventContext.getMetadata().put("resolvedVar", "resolvedValue");
            return null;
        }).when(chain).resolve(any(), any());

        interceptor.intercept(eventContext, variableContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        VariableTraceEntry entry = trace.getTrace().getVariables().get(0);

        TraceSnapshot after = entry.getAfter();
        assertThat(after).isNotNull();
        assertThat(after.getEventSnapshot()).containsKey("resolvedVar");
    }

    @Test
    void testIntercept_handlesNullAlias() {
        Map<String, Object> data = new HashMap<>();
        data.put("value", "testValue");
        variableContext = new VariableContext(null, null, "basic", data);

        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, variableContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        VariableTraceEntry entry = trace.getTrace().getVariables().get(0);

        assertThat(entry.getAlias()).isNull();
    }
}
