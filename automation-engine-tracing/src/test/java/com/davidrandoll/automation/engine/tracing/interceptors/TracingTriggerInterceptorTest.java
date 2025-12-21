package com.davidrandoll.automation.engine.tracing.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerChain;
import com.davidrandoll.automation.engine.tracing.TestEvent;
import com.davidrandoll.automation.engine.tracing.TraceContext;
import com.davidrandoll.automation.engine.tracing.TriggerTraceEntry;
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
class TracingTriggerInterceptorTest {

    @Mock
    private ITriggerChain chain;

    private TracingTriggerInterceptor interceptor;
    private EventContext eventContext;
    private TriggerContext triggerContext;

    @BeforeEach
    void setUp() {
        interceptor = new TracingTriggerInterceptor();
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);

        Map<String, Object> data = new HashMap<>();
        triggerContext = new TriggerContext("myTrigger", null, "alwaysTrueTrigger", data);
    }

    @Test
    void testIntercept_proceedsWhenTracingNotEnabled() {
        when(chain.isTriggered(eventContext, triggerContext)).thenReturn(true);

        boolean result = interceptor.intercept(eventContext, triggerContext, chain);

        verify(chain).isTriggered(eventContext, triggerContext);
        assertThat(result).isTrue();
    }

    @Test
    void testIntercept_capturesActivatedTrigger() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isTriggered(any(), any())).thenReturn(true);

        boolean result = interceptor.intercept(eventContext, triggerContext, chain);

        assertThat(result).isTrue();

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();

        assertThat(trace.getTrace().getTriggers()).hasSize(1);
        TriggerTraceEntry entry = trace.getTrace().getTriggers().get(0);
        assertThat(entry.getType()).isEqualTo("alwaysTrueTrigger");
        assertThat(entry.getAlias()).isEqualTo("myTrigger");
        assertThat(entry.isActivated()).isTrue();
    }

    @Test
    void testIntercept_capturesNotActivatedTrigger() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isTriggered(any(), any())).thenReturn(false);

        boolean result = interceptor.intercept(eventContext, triggerContext, chain);

        assertThat(result).isFalse();

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        TriggerTraceEntry entry = trace.getTrace().getTriggers().get(0);

        assertThat(entry.isActivated()).isFalse();
    }

    @Test
    void testIntercept_capturesBeforeAndAfterSnapshots() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isTriggered(any(), any())).thenReturn(true);

        interceptor.intercept(eventContext, triggerContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        TriggerTraceEntry entry = trace.getTrace().getTriggers().get(0);

        assertThat(entry.getBefore()).isNotNull();
        assertThat(entry.getAfter()).isNotNull();
        assertThat(entry.getBefore().getEventSnapshot()).containsKey("eventType");
        assertThat(entry.getBefore().getContextSnapshot()).doesNotContainKey("__type");
    }

    @Test
    void testIntercept_recordsTimingInformation() {
        TraceContext.getOrCreate(eventContext, "test-automation");
        when(chain.isTriggered(any(), any())).thenReturn(true);

        interceptor.intercept(eventContext, triggerContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        TriggerTraceEntry entry = trace.getTrace().getTriggers().get(0);

        assertThat(entry.getStartedAt()).isGreaterThan(0);
        assertThat(entry.getFinishedAt()).isGreaterThanOrEqualTo(entry.getStartedAt());
    }
}
