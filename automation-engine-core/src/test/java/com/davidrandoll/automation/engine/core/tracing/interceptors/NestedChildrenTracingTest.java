package com.davidrandoll.automation.engine.core.tracing.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionChain;
import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.tracing.*;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerChain;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import com.davidrandoll.automation.engine.core.variables.interceptors.IVariableChain;
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
import static org.mockito.Mockito.when;

/**
 * Tests for nested children tracing functionality across all component types.
 */
@ExtendWith(MockitoExtension.class)
class NestedChildrenTracingTest {

    @Mock
    private IActionChain actionChain;
    @Mock
    private IVariableChain variableChain;
    @Mock
    private ITriggerChain triggerChain;
    @Mock
    private IConditionChain conditionChain;

    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);
        TraceContext.getOrCreate(eventContext, "test-automation");
    }

    @Test
    void testActionWithNestedChildAction() {
        TracingActionInterceptor parentInterceptor = new TracingActionInterceptor();
        TracingActionInterceptor childInterceptor = new TracingActionInterceptor();

        ActionContext parentContext = new ActionContext("parent-action", null, "parentAction", Map.of());

        ActionContext childContext = new ActionContext("child-action", null, "childAction", Map.of());

        doAnswer(invocation -> {
            childInterceptor.intercept(eventContext, childContext, (ec, ac) -> {
            });
            return null;
        }).when(actionChain).execute(any(), any());

        parentInterceptor.intercept(eventContext, parentContext, actionChain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var traceResult = traceContext.complete();

        assertThat(traceResult.getTrace().getActions()).hasSize(1);
        ActionTraceEntry parentEntry = traceResult.getTrace().getActions().get(0);
        assertThat(parentEntry.getAlias()).isEqualTo("parent-action");
        assertThat(parentEntry.getChildren()).isNotNull();
        assertThat(parentEntry.getChildren().getActions()).hasSize(1);
        assertThat(parentEntry.getChildren().getActions().get(0).getAlias()).isEqualTo("child-action");
    }

    @Test
    void testVariableWithNestedChildVariable() {
        TracingVariableInterceptor parentInterceptor = new TracingVariableInterceptor();
        TracingVariableInterceptor childInterceptor = new TracingVariableInterceptor();

        VariableContext parentContext = new VariableContext("parent-variable", null, "parentVariable", Map.of());

        Map<String, Object> childData = new HashMap<>();
        childData.put("__type", "childVariable");
        childData.put("alias", "child-variable");
        VariableContext childContext = new VariableContext(childData);

        doAnswer(invocation -> {
            childInterceptor.intercept(eventContext, childContext, (ec, vc) -> {
            });
            return null;
        }).when(variableChain).resolve(any(), any());

        parentInterceptor.intercept(eventContext, parentContext, variableChain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var traceResult = traceContext.complete();

        assertThat(traceResult.getTrace().getVariables()).hasSize(1);
        VariableTraceEntry parentEntry = traceResult.getTrace().getVariables().get(0);
        assertThat(parentEntry.getAlias()).isEqualTo("parent-variable");
        assertThat(parentEntry.getChildren()).isNotNull();
        assertThat(parentEntry.getChildren().getVariables()).hasSize(1);
        assertThat(parentEntry.getChildren().getVariables().get(0).getAlias()).isEqualTo("child-variable");
    }

    @Test
    void testTriggerWithNestedChildTrigger() {
        TracingTriggerInterceptor parentInterceptor = new TracingTriggerInterceptor();
        TracingTriggerInterceptor childInterceptor = new TracingTriggerInterceptor();

        Map<String, Object> parentData = new HashMap<>();
        parentData.put("__type", "parentTrigger");
        parentData.put("alias", "parent-trigger");
        TriggerContext parentContext = new TriggerContext(parentData);

        Map<String, Object> childData = new HashMap<>();
        childData.put("__type", "childTrigger");
        childData.put("alias", "child-trigger");
        TriggerContext childContext = new TriggerContext(childData);

        when(triggerChain.isTriggered(any(), any())).thenAnswer(invocation -> {
            childInterceptor.intercept(eventContext, childContext, (ec, tc) -> true);
            return true;
        });

        parentInterceptor.intercept(eventContext, parentContext, triggerChain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var traceResult = traceContext.complete();

        assertThat(traceResult.getTrace().getTriggers()).hasSize(1);
        TriggerTraceEntry parentEntry = traceResult.getTrace().getTriggers().get(0);
        assertThat(parentEntry.getAlias()).isEqualTo("parent-trigger");
        assertThat(parentEntry.getChildren()).isNotNull();
        assertThat(parentEntry.getChildren().getTriggers()).hasSize(1);
        assertThat(parentEntry.getChildren().getTriggers().get(0).getAlias()).isEqualTo("child-trigger");
    }

    @Test
    void testConditionWithNestedChildCondition() {
        TracingConditionInterceptor parentInterceptor = new TracingConditionInterceptor();
        TracingConditionInterceptor childInterceptor = new TracingConditionInterceptor();

        Map<String, Object> parentData = new HashMap<>();
        parentData.put("__type", "parentCondition");
        parentData.put("alias", "parent-condition");
        ConditionContext parentContext = new ConditionContext(parentData);

        Map<String, Object> childData = new HashMap<>();
        childData.put("__type", "childCondition");
        childData.put("alias", "child-condition");
        ConditionContext childContext = new ConditionContext(childData);

        when(conditionChain.isSatisfied(any(), any())).thenAnswer(invocation -> {
            childInterceptor.intercept(eventContext, childContext, (ec, cc) -> true);
            return true;
        });

        parentInterceptor.intercept(eventContext, parentContext, conditionChain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var traceResult = traceContext.complete();

        assertThat(traceResult.getTrace().getConditions()).hasSize(1);
        ConditionTraceEntry parentEntry = traceResult.getTrace().getConditions().get(0);
        assertThat(parentEntry.getAlias()).isEqualTo("parent-condition");
        assertThat(parentEntry.getChildren()).isNotNull();
        assertThat(parentEntry.getChildren().getConditions()).hasSize(1);
        assertThat(parentEntry.getChildren().getConditions().get(0).getAlias()).isEqualTo("child-condition");
    }

    @Test
    void testEmptyChildrenNotAttached() {
        TracingActionInterceptor interceptor = new TracingActionInterceptor();

        Map<String, Object> data = new HashMap<>();
        data.put("__type", "simpleAction");
        data.put("alias", "simple-action");
        ActionContext context = new ActionContext(data);

        doAnswer(invocation -> null).when(actionChain).execute(any(), any());

        interceptor.intercept(eventContext, context, actionChain);

        TraceContext traceContext = TraceContext.get(eventContext);
        var traceResult = traceContext.complete();

        assertThat(traceResult.getTrace().getActions()).hasSize(1);
        ActionTraceEntry entry = traceResult.getTrace().getActions().get(0);
        assertThat(entry.getAlias()).isEqualTo("simple-action");
        assertThat(entry.getChildren()).isNull();
    }
}

