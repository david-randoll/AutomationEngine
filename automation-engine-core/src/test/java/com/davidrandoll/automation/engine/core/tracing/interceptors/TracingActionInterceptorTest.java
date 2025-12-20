package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionChain;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.tracing.ActionTraceEntry;
import com.davidrandoll.automation.engine.core.tracing.ConditionTraceEntry;
import com.davidrandoll.automation.engine.core.tracing.TraceContext;
import com.davidrandoll.automation.engine.test.TestEvent;
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
class TracingActionInterceptorTest {

    @Mock
    private IActionChain chain;

    private TracingActionInterceptor interceptor;
    private EventContext eventContext;
    private ActionContext actionContext;

    @BeforeEach
    void setUp() {
        interceptor = new TracingActionInterceptor();
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);

        Map<String, Object> data = new HashMap<>();
        data.put("__type", "loggerAction");
        data.put("alias", "logUserInfo");
        data.put("message", "User logged in");
        actionContext = new ActionContext(data);
    }

    @Test
    void testIntercept_proceedsWhenTracingNotEnabled() {
        interceptor.intercept(eventContext, actionContext, chain);

        verify(chain).execute(eventContext, actionContext);
    }

    @Test
    void testIntercept_capturesActionTraceEntry() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, actionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();

        assertThat(trace.getTrace().getActions()).hasSize(1);
        ActionTraceEntry entry = trace.getTrace().getActions().get(0);
        assertThat(entry.getType()).isEqualTo("loggerAction");
        assertThat(entry.getAlias()).isEqualTo("logUserInfo");
    }

    @Test
    void testIntercept_capturesBeforeAndAfterSnapshots() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, actionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ActionTraceEntry entry = trace.getTrace().getActions().get(0);

        assertThat(entry.getBefore()).isNotNull();
        assertThat(entry.getAfter()).isNotNull();
        assertThat(entry.getBefore().getContextSnapshot())
                .containsEntry("message", "User logged in");
    }

    @Test
    void testIntercept_filtersInternalKeys() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, actionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ActionTraceEntry entry = trace.getTrace().getActions().get(0);

        assertThat(entry.getBefore().getContextSnapshot()).doesNotContainKey("__type");
    }

    @Test
    void testIntercept_capturesNestedChildren() {
        TraceContext traceCtx = TraceContext.getOrCreate(eventContext, "test-automation");

        // Simulate a composite action that adds nested entries during execution
        doAnswer(invocation -> {
            // Simulate adding a nested condition during action execution
            ConditionTraceEntry nestedCondition = ConditionTraceEntry.builder()
                    .type("expression")
                    .satisfied(true)
                    .startedAt(System.currentTimeMillis())
                    .finishedAt(System.currentTimeMillis())
                    .build();
            traceCtx.addCondition(nestedCondition);
            return null;
        }).when(chain).execute(any(), any());

        interceptor.intercept(eventContext, actionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ActionTraceEntry entry = trace.getTrace().getActions().get(0);

        // Should have children with the nested condition
        assertThat(entry.getChildren()).isNotNull();
        assertThat(entry.getChildren().getConditions()).hasSize(1);
        assertThat(entry.getChildren().getConditions().get(0).getType()).isEqualTo("expression");
    }

    @Test
    void testIntercept_childrenNullWhenNoNested() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, actionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ActionTraceEntry entry = trace.getTrace().getActions().get(0);

        // Should be null when no children were added
        assertThat(entry.getChildren()).isNull();
    }

    @Test
    void testIntercept_recordsTimingInformation() {
        TraceContext.getOrCreate(eventContext, "test-automation");

        interceptor.intercept(eventContext, actionContext, chain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var trace = traceContext.complete();
        ActionTraceEntry entry = trace.getTrace().getActions().get(0);

        assertThat(entry.getStartedAt()).isGreaterThan(0);
        assertThat(entry.getFinishedAt()).isGreaterThanOrEqualTo(entry.getStartedAt());
    }
}
