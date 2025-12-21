package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.tracing.ExecutionTrace;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for automation tracing using executeAutomationWithYaml.
 * These tests verify that the tracing system correctly captures execution
 * traces
 * when tracing is enabled.
 */
class AutomationTracingIntegrationTest extends AutomationEngineTest {

  @Test
  void testExecuteAutomationWithYaml_tracingEnabled_capturesFullTrace() {
    var yaml = """
        alias: Test Tracing Enabled
        options:
          tracing: true
        triggers:
          - trigger: time
            at: "14:00"
        actions:
          - action: variable
            myVar: "Hello from 14:00"
          - action: logger
            message: "{{ myVar }}"
        result:
          result: basic
          value: "Execution completed"
        """;

    TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(14, 0));
    AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

    assertThat(result.isExecuted()).isTrue();
    assertThat(result.getAdditionalFields()).containsKey("trace");

    ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get("trace");
    assertThat(trace).isNotNull();
    assertThat(trace.getAlias()).isEqualTo("Test Tracing Enabled");
    assertThat(trace.getExecutionId()).isNotNull();
    assertThat(trace.getStartedAt()).isGreaterThan(0);
    assertThat(trace.getFinishedAt()).isGreaterThanOrEqualTo(trace.getStartedAt());

    // Verify trace data structure
    assertThat(trace.getTrace()).isNotNull();
    assertThat(trace.getTrace().getTriggers()).hasSize(1);
    assertThat(trace.getTrace().getActions()).hasSize(2);

    // Verify trigger was activated
    assertThat(trace.getTrace().getTriggers().get(0).isActivated()).isTrue();

    // Verify actions have correct types
    assertThat(trace.getTrace().getActions().get(0).getType()).isEqualTo("variable");
    assertThat(trace.getTrace().getActions().get(1).getType()).isEqualTo("logger");
  }

  @Test
  void testExecuteAutomationWithYaml_tracingDisabled_noTrace() {
    var yaml = """
        alias: Test Tracing Disabled
        options:
          tracing: false
        triggers:
          - trigger: time
            at: "15:00"
        actions:
          - action: logger
            message: "No trace should be captured"
        """;

    TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(15, 0));
    AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

    assertThat(result.isExecuted()).isTrue();
    assertThat(result.getAdditionalFields()).doesNotContainKey("trace");
  }

  @Test
  void testExecuteAutomationWithYaml_tracingEnabledButNoTriggerMatch_capturesSkippedExecution() {
    var yaml = """
        alias: Test Tracing Skipped Execution
        options:
          tracing: true
        triggers:
          - trigger: time
            at: "16:00"
        actions:
          - action: logger
            message: "This should not execute"
        """;

    TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(15, 0)); // Different time
    AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

    assertThat(result.isExecuted()).isFalse();
    assertThat(result.getAdditionalFields()).containsKey("trace");

    ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get("trace");
    assertThat(trace).isNotNull();
    assertThat(trace.getTrace().getTriggers().getFirst().isActivated()).isFalse();
  }

  @Test
  void testExecuteAutomationWithYaml_tracingCapturesBeforeAndAfterSnapshots() {
    var yaml = """
        alias: Test Snapshot Capture
        options:
          tracing: true
        variables:
          - variable: basic
            counter: 0
        triggers:
          - trigger: time
            at: "17:00"
        actions:
          - action: variable
            counter: 1
          - action: variable
            counter: 2
        """;

    TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(17, 0));
    AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

    assertThat(result.isExecuted()).isTrue();

    ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get("trace");
    assertThat(trace).isNotNull();

    // Verify that before/after snapshots exist
    var firstAction = trace.getTrace().getActions().get(0);
    assertThat(firstAction.getBefore()).isNotNull();
    assertThat(firstAction.getAfter()).isNotNull();
    assertThat(firstAction.getBefore().getContextSnapshot()).containsKey("counter");
    assertThat(firstAction.getAfter().getContextSnapshot()).containsKey("counter");
  }

  @Test
  void testExecuteAutomationWithYaml_tracingCapturesFailedCondition() {
    var yaml = """
        alias: Test Failed Condition
        options:
          tracing: true
        triggers:
          - trigger: time
            at: "18:00"
        conditions:
          - condition: template
            expression: "false"
        actions:
          - action: logger
            message: "Should not execute"
        """;

    TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(18, 0));
    AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

    assertThat(result.isExecuted()).isFalse();

    ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get("trace");
    assertThat(trace).isNotNull();
    assertThat(trace.getTrace().getTriggers().get(0).isActivated()).isTrue();
    assertThat(trace.getTrace().getConditions().get(0).isSatisfied()).isFalse();
    assertThat(trace.getTrace().getActions()).isEmpty(); // Actions were not executed
  }

  @Test
  void testExecuteAutomationWithYaml_tracingCapturesMultipleVariables() {
    var yaml = """
        alias: Test Multiple Variables
        options:
          tracing: true
        variables:
          - variable: basic
            var1: "value1"
          - variable: basic
            var2: "value2"
          - variable: basic
            var3: "{{ var1 }} and {{ var2 }}"
        triggers:
          - trigger: time
            at: "19:00"
        actions:
          - action: logger
            message: "{{ var3 }}"
        """;

    TimeBasedEvent event = new TimeBasedEvent(LocalTime.of(19, 0));
    AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

    assertThat(result.isExecuted()).isTrue();

    ExecutionTrace trace = (ExecutionTrace) result.getAdditionalFields().get("trace");
    assertThat(trace).isNotNull();
    assertThat(trace.getTrace().getVariables()).hasSize(3);

    // Verify variable types are captured
    trace.getTrace().getVariables().forEach(varEntry -> {
      assertThat(varEntry.getType()).isEqualTo("basic");
      // Note: alias is optional for basic variables, so we don't assert on it
    });
  }
}
