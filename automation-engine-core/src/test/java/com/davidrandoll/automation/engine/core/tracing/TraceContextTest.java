package com.davidrandoll.automation.engine.core.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraceContextTest {

    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .build();
        eventContext = new EventContext(event);
    }

    @Test
    void testGetOrCreate_createsNewContext() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test-automation");

        assertThat(traceContext).isNotNull();
        assertThat(traceContext.getExecutionTrace().getAlias()).isEqualTo("test-automation");
        assertThat(traceContext.getExecutionTrace().getExecutionId()).isNotNull();
        assertThat(traceContext.getExecutionTrace().getStartedAt()).isGreaterThan(0);
    }

    @Test
    void testGetOrCreate_returnsExistingContext() {
        TraceContext first = TraceContext.getOrCreate(eventContext, "first");
        TraceContext second = TraceContext.getOrCreate(eventContext, "second");

        assertThat(second).isSameAs(first);
        assertThat(second.getExecutionTrace().getAlias()).isEqualTo("first");
    }

    @Test
    void testGet_returnsNullWhenNotPresent() {
        TraceContext traceContext = TraceContext.get(eventContext);

        assertThat(traceContext).isNull();
    }

    @Test
    void testGet_returnsContextWhenPresent() {
        TraceContext created = TraceContext.getOrCreate(eventContext, "test");
        TraceContext retrieved = TraceContext.get(eventContext);

        assertThat(retrieved).isSameAs(created);
    }

    @Test
    void testIsTracingEnabled_returnsFalseWhenNotPresent() {
        boolean enabled = TraceContext.isTracingEnabled(eventContext);

        assertThat(enabled).isFalse();
    }

    @Test
    void testIsTracingEnabled_returnsTrueWhenPresent() {
        TraceContext.getOrCreate(eventContext, "test");

        boolean enabled = TraceContext.isTracingEnabled(eventContext);

        assertThat(enabled).isTrue();
    }

    @Test
    void testAddVariable_addsToRootScope() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");
        VariableTraceEntry entry = VariableTraceEntry.builder()
                .type("basic")
                .alias("myVar")
                .startedAt(1000)
                .finishedAt(1001)
                .build();

        traceContext.addVariable(entry);
        ExecutionTrace result = traceContext.complete();

        assertThat(result.getTrace().getVariables()).hasSize(1);
        assertThat(result.getTrace().getVariables().get(0).getType()).isEqualTo("basic");
    }

    @Test
    void testAddTrigger_addsToRootScope() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");
        TriggerTraceEntry entry = TriggerTraceEntry.builder()
                .type("alwaysTrueTrigger")
                .activated(true)
                .startedAt(1000)
                .finishedAt(1001)
                .build();

        traceContext.addTrigger(entry);
        ExecutionTrace result = traceContext.complete();

        assertThat(result.getTrace().getTriggers()).hasSize(1);
        assertThat(result.getTrace().getTriggers().get(0).isActivated()).isTrue();
    }

    @Test
    void testAddCondition_addsToRootScope() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");
        ConditionTraceEntry entry = ConditionTraceEntry.builder()
                .type("expression")
                .satisfied(true)
                .startedAt(1000)
                .finishedAt(1001)
                .build();

        traceContext.addCondition(entry);
        ExecutionTrace result = traceContext.complete();

        assertThat(result.getTrace().getConditions()).hasSize(1);
        assertThat(result.getTrace().getConditions().get(0).isSatisfied()).isTrue();
    }

    @Test
    void testAddAction_addsToRootScope() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");
        ActionTraceEntry entry = ActionTraceEntry.builder()
                .type("loggerAction")
                .startedAt(1000)
                .finishedAt(1001)
                .build();

        traceContext.addAction(entry);
        ExecutionTrace result = traceContext.complete();

        assertThat(result.getTrace().getActions()).hasSize(1);
        assertThat(result.getTrace().getActions().get(0).getType()).isEqualTo("loggerAction");
    }

    @Test
    void testSetResult_setsOnRootScope() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");
        ResultTraceEntry entry = ResultTraceEntry.builder()
                .result("test result")
                .startedAt(1000)
                .finishedAt(1001)
                .build();

        traceContext.setResult(entry);
        ExecutionTrace result = traceContext.complete();

        assertThat(result.getTrace().getResult()).isNotNull();
        assertThat(result.getTrace().getResult().getResult()).isEqualTo("test result");
    }

    @Test
    void testNestedScope_entriesGoToChildren() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");

        // Enter nested scope (like entering an ifThenElse action)
        TraceChildren children = traceContext.enterNestedScope();

        // Add entry in nested scope
        ConditionTraceEntry nestedEntry = ConditionTraceEntry.builder()
                .type("expression")
                .satisfied(true)
                .startedAt(2000)
                .finishedAt(2001)
                .build();
        traceContext.addCondition(nestedEntry);

        // Exit nested scope
        traceContext.exitNestedScope();

        // Add entry at root level
        ActionTraceEntry actionEntry = ActionTraceEntry.builder()
                .type("ifThenElse")
                .children(children)
                .startedAt(1000)
                .finishedAt(3000)
                .build();
        traceContext.addAction(actionEntry);

        ExecutionTrace result = traceContext.complete();

        // Root should have one action
        assertThat(result.getTrace().getActions()).hasSize(1);
        assertThat(result.getTrace().getConditions()).isEmpty();

        // The action should have nested condition in children
        ActionTraceEntry action = result.getTrace().getActions().get(0);
        assertThat(action.getChildren()).isNotNull();
        assertThat(action.getChildren().getConditions()).hasSize(1);
        assertThat(action.getChildren().getConditions().get(0).isSatisfied()).isTrue();
    }

    @Test
    void testComplete_setsFinishedAt() {
        TraceContext traceContext = TraceContext.getOrCreate(eventContext, "test");

        ExecutionTrace result = traceContext.complete();

        assertThat(result.getFinishedAt()).isGreaterThan(0);
        assertThat(result.getFinishedAt()).isGreaterThanOrEqualTo(result.getStartedAt());
    }
}
