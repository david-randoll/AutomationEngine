package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.spring.AESpringConfig;
import com.davidrandoll.automation.engine.spring.config.TracingConfig;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for automation execution tracing.
 */
@SpringBootTest(classes = {AESpringConfig.class, TracingConfig.class, TestConfig.class})
@TestPropertySource(properties = {
        "automation-engine.tracing.enabled=true",
        "automation-engine.tracing.include-context-snapshots=true",
        "automation-engine.tracing.snapshot-mode=KEYS_ONLY"
})
class AutomationTracingIntegrationTest {

    @Autowired
    private AutomationEngine engine;

    @Test
    void shouldTraceSimpleAutomationExecution() {
        // Given: Simple automation with one action
        String yaml = """
                alias: test-automation
                triggers:
                  - trigger: alwaysTrueTrigger
                actions:
                  - action: loggerAction
                    message: "Hello World"
                """;

        IEvent event = new TestEvent("test");

        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // Then: Result contains trace data
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getAdditionalFields()).isNotEmpty();
        assertThat(result.getAdditionalFields()).containsKey("trace");

        // Extract trace
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        assertThat(trace).isNotEmpty();

        // Verify trace ID
        String traceId = AutomationTraceExtractor.getTraceId(result);
        assertThat(traceId).isNotNull();
        assertThat(trace.get("traceId")).isEqualTo(traceId);

        // Verify basic info
        assertThat(trace.get("automationAlias")).isEqualTo("test-automation");
        assertThat(trace.get("executed")).isEqualTo(true);

        // Verify timing
        assertThat(trace).containsKey("timing");
        @SuppressWarnings("unchecked")
        Map<String, Object> timing = (Map<String, Object>) trace.get("timing");
        assertThat(timing).containsKeys("startNanos", "endNanos", "durationNanos", "durationMillis");
        assertThat(timing.get("durationNanos")).isInstanceOf(Long.class);
        assertThat((Long) timing.get("durationNanos")).isPositive();

        // Verify duration helper
        Double durationMillis = AutomationTraceExtractor.getDurationMillis(result);
        assertThat(durationMillis).isNotNull().isPositive();
    }

    @Test
    void shouldTraceTriggersAndConditions() {
        // Given: Automation with multiple triggers and conditions
        String yaml = """
                alias: test-conditions
                triggers:
                  - trigger: alwaysTrueTrigger
                conditions:
                  - condition: alwaysTrueCondition
                actions:
                  - action: loggerAction
                    message: "Conditions met"
                """;

        IEvent event = new TestEvent("test");

        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // Then: Trace contains trigger and condition data
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);

        // Verify triggers
        assertThat(trace).containsKey("triggers");
        List<Map<String, Object>> triggers = AutomationTraceExtractor.getTriggers(result);
        assertThat(triggers).hasSize(1);

        Map<String, Object> trigger = triggers.get(0);
        assertThat(trigger).containsKeys("alias", "type", "activated", "durationNanos");
        assertThat(trigger.get("activated")).isEqualTo(true);
        assertThat(trigger.get("durationNanos")).isInstanceOf(Long.class);

        // Verify trigger summary
        assertThat(trace.get("triggersActivated")).isEqualTo(1L);

        // Verify conditions
        assertThat(trace).containsKey("conditions");
        List<Map<String, Object>> conditions = AutomationTraceExtractor.getConditions(result);
        assertThat(conditions).hasSize(1);

        Map<String, Object> condition = conditions.get(0);
        assertThat(condition).containsKeys("alias", "type", "satisfied", "durationNanos");
        assertThat(condition.get("satisfied")).isEqualTo(true);

        // Verify condition summary
        assertThat(trace.get("allConditionsSatisfied")).isEqualTo(true);
    }

    @Test
    void shouldTraceActions() {
        // Given: Automation with multiple actions
        String yaml = """
                alias: test-actions
                triggers:
                  - trigger: alwaysTrueTrigger
                actions:
                  - action: loggerAction
                    alias: first-log
                    message: "First"
                  - action: loggerAction
                    alias: second-log
                    message: "Second"
                """;

        IEvent event = new TestEvent("test");

        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // Then: Trace contains action data
        List<Map<String, Object>> actions = AutomationTraceExtractor.getActions(result);
        assertThat(actions).hasSize(2);

        // Verify first action
        Map<String, Object> firstAction = actions.get(0);
        assertThat(firstAction).containsKeys("alias", "type", "durationNanos");
        assertThat(firstAction.get("alias")).isEqualTo("first-log");
        assertThat(firstAction.get("durationNanos")).isInstanceOf(Long.class);
        assertThat(firstAction).doesNotContainKey("exception");

        // Verify second action
        Map<String, Object> secondAction = actions.get(1);
        assertThat(secondAction.get("alias")).isEqualTo("second-log");

        // Verify no exceptions
        assertThat(AutomationTraceExtractor.hasActionExceptions(result)).isFalse();
    }

    @Test
    void shouldTraceVariables() {
        // Given: Automation with variables
        String yaml = """
                alias: test-variables
                variables:
                  - alias: myVar
                    myVar: "test-value"
                  - alias: computed
                    computed: "{{ myVar }}-computed"
                triggers:
                  - trigger: alwaysTrueTrigger
                actions:
                  - action: loggerAction
                    message: "{{ computed }}"
                """;

        IEvent event = new TestEvent("test");

        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // Then: Trace contains variable data
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        assertThat(trace).containsKey("variables");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> variables = (List<Map<String, Object>>) trace.get("variables");
        assertThat(variables).hasSize(2);

        // Verify first variable
        Map<String, Object> firstVar = variables.get(0);
        assertThat(firstVar).containsKeys("alias", "type", "beforeValue", "afterValue", "durationNanos");
        assertThat(firstVar.get("alias")).isEqualTo("myVar");
        assertThat(firstVar.get("afterValue")).isEqualTo("test-value");
    }

    @Test
    void shouldTraceSkippedAutomation() {
        // Given: Automation that won't execute (no triggers activated)
        String yaml = """
                alias: skipped-automation
                triggers:
                      - trigger: alwaysFalseTrigger
                """;

        IEvent event = new TestEvent("test");

        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // Then: Automation skipped but still traced
        assertThat(result.isExecuted()).isFalse();

        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        assertThat(trace.get("executed")).isEqualTo(false);

        // Verify triggers were evaluated
        assertThat(trace).containsKey("triggers");
        List<Map<String, Object>> triggers = AutomationTraceExtractor.getTriggers(result);
        assertThat(triggers).isNotEmpty();

        // No actions should be traced (not executed)
        assertThat(trace).doesNotContainKey("actions");
    }

    @Test
    void shouldIncludeContextSnapshots() {
        // Given: Automation with variables
        String yaml = """
                alias: snapshot-test
                variables:
                  - alias: testVar
                    value: "initial"
                triggers:
                  - trigger: alwaysTrueTrigger
                actions:
                  - action: variableAction
                    resultVar: "modified"
                """;

        IEvent event = new TestEvent("test");

        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // Then: Trace contains context snapshots
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        assertThat(trace).containsKeys("contextSnapshotBefore", "contextSnapshotAfter");

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshotBefore = (Map<String, Object>) trace.get("contextSnapshotBefore");
        assertThat(snapshotBefore).containsKey("metadataKeys");

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshotAfter = (Map<String, Object>) trace.get("contextSnapshotAfter");
        assertThat(snapshotAfter).containsKey("metadataKeys");
    }

    @Test
    void shouldPrettyPrintTrace() {
        // Given: Automation execution with trace
        String yaml = """
                alias: print-test
                triggers:
                  - trigger: alwaysTrueTrigger
                actions:
                  - action: loggerAction
                    message: "Test"
                """;

        IEvent event = new TestEvent("test");
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // When: Pretty print trace
        String printed = AutomationTracePrinter.prettyPrint(result);

        // Then: Output contains key trace information
        assertThat(printed).contains("AUTOMATION EXECUTION TRACE");
        assertThat(printed).contains("print-test");
        assertThat(printed).contains("TIMING");
        assertThat(printed).contains("Duration");
        assertThat(printed).contains("TRIGGERS");
        assertThat(printed).contains("ACTIONS");
    }

    @Test
    void shouldGenerateSummary() {
        // Given: Automation execution with trace
        String yaml = """
                alias: summary-test
                triggers:
                  - trigger: alwaysTrueTrigger
                actions:
                  - action: loggerAction
                    message: "Test"
                """;

        IEvent event = new TestEvent("test");
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

        // When: Generate summary
        String summary = AutomationTracePrinter.summary(result);

        // Then: Summary contains key information
        assertThat(summary).contains("[Trace");
        assertThat(summary).contains("summary-test");
        assertThat(summary).contains("Executed: true");
        assertThat(summary).containsPattern("\\d+\\.\\d+ms");
    }

    @Data
    static class TestEvent implements IEvent {
        private final String name;
    }
}
