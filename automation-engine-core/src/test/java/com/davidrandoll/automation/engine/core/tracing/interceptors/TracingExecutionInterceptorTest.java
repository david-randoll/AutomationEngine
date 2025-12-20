package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.tracing.ExecutionTrace;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.test.TestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingExecutionInterceptorTest {

    private Automation automation;

    @Mock
    private IAutomationExecutionChain chain;

    private TracingExecutionInterceptor interceptor;
    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        interceptor = new TracingExecutionInterceptor(true);
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);
        // Create a real automation instead of mock to avoid copy constructor issues
        automation = new Automation("test-automation", null, null, null, null, null);
    }

    @Test
    void testIntercept_attachesTraceToResult() {
        AutomationResult originalResult = AutomationResult.executed(automation, eventContext, "result value");
        when(chain.proceed(any(), any())).thenReturn(originalResult);

        AutomationResult result = interceptor.intercept(automation, eventContext, chain);

        assertThat(result).isNotNull();
        assertThat(result.getAdditionalFields()).containsKey(ExecutionTrace.TRACE_KEY);

        ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get(ExecutionTrace.TRACE_KEY);
        assertThat(trace.getAlias()).isEqualTo("test-automation");
        assertThat(trace.getExecutionId()).isNotNull();
        assertThat(trace.getStartedAt()).isGreaterThan(0);
        assertThat(trace.getFinishedAt()).isGreaterThan(0);
    }

    @Test
    void testIntercept_preservesOriginalResultValue() {
        AutomationResult originalResult = AutomationResult.executed(automation, eventContext, "my result");
        when(chain.proceed(any(), any())).thenReturn(originalResult);

        AutomationResult result = interceptor.intercept(automation, eventContext, chain);

        assertThat(result.orElse(null)).isEqualTo("my result");
        assertThat(result.isExecuted()).isTrue();
    }

    @Test
    void testIntercept_preservesSkippedStatus() {
        AutomationResult originalResult = AutomationResult.skipped(automation, eventContext);
        when(chain.proceed(any(), any())).thenReturn(originalResult);

        AutomationResult result = interceptor.intercept(automation, eventContext, chain);

        assertThat(result.isExecuted()).isFalse();
        assertThat(result.getAdditionalFields()).containsKey(ExecutionTrace.TRACE_KEY);
    }

    @Test
    void testIntercept_preservesExistingAdditionalFields() {
        Map<String, Object> existingFields = Map.of("customField", "customValue");
        AutomationResult originalResult = AutomationResult.executedWithAdditionalFields(
                automation, eventContext, "result", true, existingFields);
        when(chain.proceed(any(), any())).thenReturn(originalResult);

        AutomationResult result = interceptor.intercept(automation, eventContext, chain);

        assertThat(result.getAdditionalFields()).containsKey("customField");
        assertThat(result.getAdditionalFields()).containsKey(ExecutionTrace.TRACE_KEY);
        assertThat(result.getAdditionalFields().get("customField")).isEqualTo("customValue");
    }

    @Test
    void testConstructor_withTracingDisabled() {
        TracingExecutionInterceptor disabledInterceptor = new TracingExecutionInterceptor(false);

        assertThat(disabledInterceptor.isTracingEnabled()).isFalse();
    }

    @Test
    void testIntercept_traceHasCorrectStructure() {
        AutomationResult originalResult = AutomationResult.executed(automation, eventContext, null);
        when(chain.proceed(any(), any())).thenReturn(originalResult);

        AutomationResult result = interceptor.intercept(automation, eventContext, chain);

        ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get(ExecutionTrace.TRACE_KEY);

        // Verify trace structure
        assertThat(trace.getTrace()).isNotNull();
        assertThat(trace.getTrace().getVariables()).isNotNull();
        assertThat(trace.getTrace().getTriggers()).isNotNull();
        assertThat(trace.getTrace().getConditions()).isNotNull();
        assertThat(trace.getTrace().getActions()).isNotNull();
    }
}
