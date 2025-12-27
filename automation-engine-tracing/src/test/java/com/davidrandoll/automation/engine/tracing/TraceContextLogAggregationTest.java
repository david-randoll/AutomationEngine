package com.davidrandoll.automation.engine.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for log capture at trace level.
 * Trace-level logs capture automation execution logs independently,
 * not aggregated from children components.
 */
class TraceContextLogAggregationTest {

    private EventContext eventContext;
    private TraceContext traceContext;

    @BeforeEach
    void setUp() {
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test event")
                .build();
        eventContext = new EventContext(event);
        traceContext = TraceContext.getOrCreate(eventContext, "log-aggregation-test");
        TraceContext.setThreadContext(traceContext);
    }

    @Test
    void testTraceLevelLogCapture_independent() {
        // Arrange - logs at trace level (outside of components)
        LogEntry traceLog1 = createLog("Automation started", "INFO");
        LogEntry traceLog2 = createLog("Processing automation", "DEBUG");
        TraceContext.recordLog(traceLog1);
        TraceContext.recordLog(traceLog2);

        // Add a component with its own logs
        List<LogEntry> actionLogs = List.of(createLog("Action log", "INFO"));
        traceContext.addAction(createActionEntry(actionLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - trace level has its own logs PLUS aggregated component logs
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).contains(traceLog1, traceLog2, actionLogs.get(0));

        // Children should still have their logs
        assertThat(result.getTrace().getActions()).hasSize(1);
        assertThat(result.getTrace().getActions().get(0).getLogs()).containsExactly(actionLogs.get(0));
    }

    @Test
    void testTraceLevelLogs_withComponentLogs() {
        // Arrange - trace level logs
        LogEntry traceLog = createLog("Trace level log", "DEBUG");
        TraceContext.recordLog(traceLog);

        // Add component with its own logs
        List<LogEntry> triggerLogs = List.of(createLog("Trigger log", "INFO"));
        traceContext.addTrigger(createTriggerEntry(triggerLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - trace logs AND component logs aggregated together
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).contains(traceLog);
        assertThat(result.getLogs()).contains(triggerLogs.get(0));
        // Component logs also remain in their entries
        assertThat(result.getTrace().getTriggers().get(0).getLogs()).containsExactly(triggerLogs.get(0));
    }

    @Test
    void testTraceLevelLogs_empty() {
        // Arrange - no trace-level logs recorded
        // Add components with their logs
        List<LogEntry> conditionLogs = List.of(createLog("Condition log", "INFO"));
        traceContext.addCondition(createConditionEntry(conditionLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - trace logs aggregated from components even if trace-level empty
        assertThat(result.getLogs()).hasSize(1);
        assertThat(result.getLogs()).contains(conditionLogs.get(0));
        // Component logs also remain in their entries
        assertThat(result.getTrace().getConditions().get(0).getLogs()).hasSize(1);
    }

    @Test
    void testComponentLogs_remainInComponentEntries() {
        // Arrange - Component logs should stay in their trace entries AND be aggregated
        List<LogEntry> actionLogs = List.of(
                createLog("Action started", "INFO"),
                createLog("Action processing", "DEBUG"),
                createLog("Action completed", "INFO"));

        ActionTraceEntry action = ActionTraceEntry.builder()
                .type("action")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(actionLogs)
                .build();

        traceContext.addAction(action);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Component logs aggregated to trace level AND remain in component entry
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).containsAll(actionLogs);
        assertThat(result.getTrace().getActions().get(0).getLogs()).hasSize(3);
        assertThat(result.getTrace().getActions().get(0).getLogs()).containsAll(actionLogs);
    }

    @Test
    void testComponentLogs_allTypesRemainInEntries() {
        // Arrange - Logs from all component types aggregated AND stay in their entries
        List<LogEntry> varLogs = List.of(createLog("Variable log", "DEBUG"));
        List<LogEntry> triggerLogs = List.of(createLog("Trigger log", "INFO"));
        List<LogEntry> conditionLogs = List.of(createLog("Condition log", "TRACE"));
        List<LogEntry> actionLogs = List.of(createLog("Action log", "INFO"));
        List<LogEntry> resultLogs = List.of(createLog("Result log", "DEBUG"));

        traceContext.addVariable(createVariableEntry("var", varLogs));
        traceContext.addTrigger(createTriggerEntry(triggerLogs));
        traceContext.addCondition(createConditionEntry(conditionLogs));
        traceContext.addAction(createActionEntry(actionLogs));
        traceContext.setResult(createResultEntry(resultLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - All component logs aggregated to trace level
        assertThat(result.getLogs()).hasSize(5);
        assertThat(result.getLogs()).containsAll(varLogs);
        assertThat(result.getLogs()).containsAll(triggerLogs);
        assertThat(result.getLogs()).containsAll(conditionLogs);
        assertThat(result.getLogs()).containsAll(actionLogs);
        assertThat(result.getLogs()).containsAll(resultLogs);
        // Component logs also remain in their entries
        assertThat(result.getTrace().getVariables().get(0).getLogs()).containsAll(varLogs);
        assertThat(result.getTrace().getTriggers().get(0).getLogs()).containsAll(triggerLogs);
        assertThat(result.getTrace().getConditions().get(0).getLogs()).containsAll(conditionLogs);
        assertThat(result.getTrace().getActions().get(0).getLogs()).containsAll(actionLogs);
        assertThat(result.getTrace().getResult().getLogs()).containsAll(resultLogs);
    }

    @Test
    void testNestedComponentLogs_remainInHierarchy() {
        // Arrange - Nested component logs aggregated recursively AND stay in their structure
        List<LogEntry> parentLogs = List.of(createLog("Parent log", "INFO"));
        List<LogEntry> childLogs = List.of(createLog("Child log", "DEBUG"));

        TraceChildren children = new TraceChildren();
        ActionTraceEntry childAction = ActionTraceEntry.builder()
                .type("childAction")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(childLogs)
                .build();
        children.getActions().add(childAction);

        ActionTraceEntry parentAction = ActionTraceEntry.builder()
                .type("parentAction")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(parentLogs)
                .children(children)
                .build();

        traceContext.addAction(parentAction);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - All logs aggregated to trace level (parent + child)
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).containsAll(parentLogs);
        assertThat(result.getLogs()).containsAll(childLogs);
        // Logs also remain in their component hierarchy
        ActionTraceEntry resultAction = result.getTrace().getActions().get(0);
        assertThat(resultAction.getLogs()).containsAll(parentLogs);
        assertThat(resultAction.getChildren().getActions().get(0).getLogs()).containsAll(childLogs);
    }

    @Test
    void testHandlesNull_andEmptyComponentLogs() {
        // Arrange
        ActionTraceEntry actionWithNull = ActionTraceEntry.builder()
                .type("actionNull")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(null)
                .build();

        ActionTraceEntry actionWithEmpty = ActionTraceEntry.builder()
                .type("actionEmpty")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(new ArrayList<>())
                .build();

        traceContext.addAction(actionWithNull);
        traceContext.addAction(actionWithEmpty);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Should not throw, aggregation handles null/empty gracefully
        assertThat(result.getLogs()).isEmpty();
    }

    private LogEntry createLog(String message, String level) {
        return createLog(message, level, System.currentTimeMillis());
    }

    private LogEntry createLog(String message, String level, long timestamp) {
        return LogEntry.builder()
                .message(message)
                .formattedMessage(message)
                .timestamp(Instant.ofEpochMilli(timestamp))
                .level(level)
                .build();
    }

    private VariableTraceEntry createVariableEntry(String alias, List<LogEntry> logs) {
        return VariableTraceEntry.builder()
                .type("variable")
                .alias(alias)
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();
    }

    private TriggerTraceEntry createTriggerEntry(List<LogEntry> logs) {
        return TriggerTraceEntry.builder()
                .type("trigger")
                .activated(true)
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();
    }

    private ConditionTraceEntry createConditionEntry(List<LogEntry> logs) {
        return ConditionTraceEntry.builder()
                .type("condition")
                .satisfied(true)
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();
    }

    private ActionTraceEntry createActionEntry(List<LogEntry> logs) {
        return ActionTraceEntry.builder()
                .type("action")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();
    }

    private ResultTraceEntry createResultEntry(List<LogEntry> logs) {
        return ResultTraceEntry.builder()
                .result("result")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();
    }
}
