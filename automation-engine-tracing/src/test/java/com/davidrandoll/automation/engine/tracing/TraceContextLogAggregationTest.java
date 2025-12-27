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

        // Simulate component logging: start capture, log, stop capture
        traceContext.startLogCapture();  // Component buffer
        LogEntry actionLog = createLog("Action log", "INFO");
        TraceContext.recordLog(actionLog);  // Goes to BOTH component AND trace buffers
        List<LogEntry> actionLogs = traceContext.stopLogCapture();
        traceContext.addAction(createActionEntry(actionLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - trace level has ALL logs (trace + component via dual-write)
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).contains(traceLog1, traceLog2, actionLog);

        // Children should still have their logs
        assertThat(result.getTrace().getActions()).hasSize(1);
        assertThat(result.getTrace().getActions().get(0).getLogs()).containsExactly(actionLog);
    }

    @Test
    void testTraceLevelLogs_withComponentLogs() {
        // Arrange - trace level logs
        LogEntry traceLog = createLog("Trace level log", "DEBUG");
        TraceContext.recordLog(traceLog);

        // Simulate component logging: start capture, log, stop capture
        traceContext.startLogCapture();  // Trigger buffer
        LogEntry triggerLog = createLog("Trigger log", "INFO");
        TraceContext.recordLog(triggerLog);  // Goes to BOTH trigger AND trace buffers
        List<LogEntry> triggerLogs = traceContext.stopLogCapture();
        traceContext.addTrigger(createTriggerEntry(triggerLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - trace logs AND component logs (via dual-write) are aggregated
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).contains(traceLog, triggerLog);
        // Component logs also remain in their entries
        assertThat(result.getTrace().getTriggers().get(0).getLogs()).containsExactly(triggerLog);
    }

    @Test
    void testTraceLevelLogs_empty() {
        // Arrange - no trace-level logs recorded
        // Simulate component logging: start capture, log, stop capture
        traceContext.startLogCapture();  // Condition buffer
        LogEntry conditionLog = createLog("Condition log", "INFO");
        TraceContext.recordLog(conditionLog);  // Goes to BOTH condition AND trace buffers
        List<LogEntry> conditionLogs = traceContext.stopLogCapture();
        traceContext.addCondition(createConditionEntry(conditionLogs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - trace logs contain component logs (via dual-write) even if no direct trace logs
        assertThat(result.getLogs()).hasSize(1);
        assertThat(result.getLogs()).contains(conditionLog);
        // Component logs also remain in their entries
        assertThat(result.getTrace().getConditions().get(0).getLogs()).hasSize(1);
    }

    @Test
    void testComponentLogs_remainInComponentEntries() {
        // Arrange - Simulate component logging using start/stop capture to trigger dual-write
        traceContext.startLogCapture();
        LogEntry log1 = createLog("Action started", "INFO");
        LogEntry log2 = createLog("Action processing", "DEBUG");
        LogEntry log3 = createLog("Action completed", "INFO");
        TraceContext.recordLog(log1);
        TraceContext.recordLog(log2);
        TraceContext.recordLog(log3);
        List<LogEntry> actionLogs = traceContext.stopLogCapture();

        ActionTraceEntry action = ActionTraceEntry.builder()
                .type("action")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(actionLogs)
                .build();

        traceContext.addAction(action);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Component logs dual-written to trace level AND remain in component entry
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).containsAll(actionLogs);
        assertThat(result.getTrace().getActions().get(0).getLogs()).hasSize(3);
        assertThat(result.getTrace().getActions().get(0).getLogs()).containsAll(actionLogs);
    }

    @Test
    void testComponentLogs_allTypesRemainInEntries() {
        // Arrange - Logs from all component types using dual-write mechanism
        // Variable
        traceContext.startLogCapture();
        LogEntry varLog = createLog("Variable log", "DEBUG");
        TraceContext.recordLog(varLog);
        traceContext.addVariable(createVariableEntry("var", traceContext.stopLogCapture()));

        // Trigger
        traceContext.startLogCapture();
        LogEntry triggerLog = createLog("Trigger log", "INFO");
        TraceContext.recordLog(triggerLog);
        traceContext.addTrigger(createTriggerEntry(traceContext.stopLogCapture()));

        // Condition
        traceContext.startLogCapture();
        LogEntry conditionLog = createLog("Condition log", "TRACE");
        TraceContext.recordLog(conditionLog);
        traceContext.addCondition(createConditionEntry(traceContext.stopLogCapture()));

        // Action
        traceContext.startLogCapture();
        LogEntry actionLog = createLog("Action log", "INFO");
        TraceContext.recordLog(actionLog);
        traceContext.addAction(createActionEntry(traceContext.stopLogCapture()));

        // Result
        traceContext.startLogCapture();
        LogEntry resultLog = createLog("Result log", "DEBUG");
        TraceContext.recordLog(resultLog);
        traceContext.setResult(createResultEntry(traceContext.stopLogCapture()));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - All component logs dual-written to trace level AND remain in their entries
        assertThat(result.getLogs()).hasSize(5);
        assertThat(result.getLogs()).contains(varLog, triggerLog, conditionLog, actionLog, resultLog);

        // Each component still has its logs
        assertThat(result.getTrace().getVariables().get(0).getLogs()).containsExactly(varLog);
        assertThat(result.getTrace().getTriggers().get(0).getLogs()).containsExactly(triggerLog);
        assertThat(result.getTrace().getConditions().get(0).getLogs()).containsExactly(conditionLog);
        assertThat(result.getTrace().getActions().get(0).getLogs()).containsExactly(actionLog);
        assertThat(result.getTrace().getResult().getLogs()).containsExactly(resultLog);
    }

    @Test
    void testNestedComponentLogs_remainInHierarchy() {
        // Arrange - Nested component logs using dual-write mechanism
        
        // Parent action
        traceContext.startLogCapture();
        LogEntry parentLog = createLog("Parent log", "INFO");
        TraceContext.recordLog(parentLog);
        List<LogEntry> parentLogs = traceContext.stopLogCapture();

        // Child action (nested scope)
        traceContext.enterNestedScope();
        traceContext.startLogCapture();
        LogEntry childLog = createLog("Child log", "DEBUG");
        TraceContext.recordLog(childLog);
        List<LogEntry> childLogs = traceContext.stopLogCapture();

        TraceChildren children = new TraceChildren();
        ActionTraceEntry childAction = ActionTraceEntry.builder()
                .type("childAction")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(childLogs)
                .build();
        children.getActions().add(childAction);
        traceContext.exitNestedScope();

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

        // Assert - All logs dual-written to trace level (parent + child)
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).contains(parentLog, childLog);
        // Logs also remain in their component hierarchy
        ActionTraceEntry resultAction = result.getTrace().getActions().get(0);
        assertThat(resultAction.getLogs()).containsExactly(parentLog);
        assertThat(resultAction.getChildren().getActions().get(0).getLogs()).containsExactly(childLog);
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
