package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.tracing.ConditionTraceEntry;
import com.davidrandoll.automation.engine.tracing.TestEvent;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingConditionInterceptorTest {

    @Mock
    private IConditionChain chain;

    private TracingConditionInterceptor interceptor;
    private EventContext eventContext;
    private ConditionContext conditionContext;

    @BeforeEach
    void setUp() {
        interceptor = new TracingConditionInterceptor();
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);

        Map<String, Object> data = new HashMap<>();
        data.put("expression", "{{ userActive == true }}");
        conditionContext = new ConditionContext("Check user active", null, "expression", data);
    }

    @Test
    void testIntercept_proceedsWhenTracingNotEnabled() {
        when(chain.isSatisfied(eventContext, conditionContext)).thenReturn(true);

        boolean result = interceptor.intercept(eventContext, conditionContext, chain);

        verify(chain).isSatisfied(eventContext, conditionContext);
        assertThat(result).isTrue();
    }

    @Test
    void testIntercept_capturesSatisfiedCondition() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isSatisfied(any(), any())).thenReturn(true);

        boolean result = interceptor.intercept(eventContext, conditionContext, chain);

        assertThat(result).isTrue();

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();

        assertThat(trace.getTrace().getConditions()).hasSize(1);
        ConditionTraceEntry entry = trace.getTrace().getConditions().get(0);
        assertThat(entry.getType()).isEqualTo("expression");
        assertThat(entry.getAlias()).isEqualTo("Check user active");
        assertThat(entry.isSatisfied()).isTrue();
    }

    @Test
    void testIntercept_capturesNotSatisfiedCondition() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isSatisfied(any(), any())).thenReturn(false);

        boolean result = interceptor.intercept(eventContext, conditionContext, chain);

        assertThat(result).isFalse();

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ConditionTraceEntry entry = trace.getTrace().getConditions().get(0);

        assertThat(entry.isSatisfied()).isFalse();
    }

    @Test
    void testIntercept_capturesExpressionInContextSnapshot() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isSatisfied(any(), any())).thenReturn(true);

        interceptor.intercept(eventContext, conditionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ConditionTraceEntry entry = trace.getTrace().getConditions().get(0);

        assertThat(entry.getBefore().getContextSnapshot())
                .containsEntry("expression", "{{ userActive == true }}");
    }

    @Test
    void testIntercept_recordsTimingInformation() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isSatisfied(any(), any())).thenReturn(true);

        interceptor.intercept(eventContext, conditionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ConditionTraceEntry entry = trace.getTrace().getConditions().get(0);

        assertThat(entry.getStartedAt()).isGreaterThan(0);
        assertThat(entry.getFinishedAt()).isGreaterThanOrEqualTo(entry.getStartedAt());
    }
}
