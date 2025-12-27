package com.davidrandoll.automation.engine.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for log collection during exception scenarios.
 * Ensures that logs are properly captured even when exceptions occur.
 * Note: Exception details are logged through the logging system, not stored as error fields.
 */
class LogCollectionWithExceptionsTest {

    private EventContext eventContext;
    private TraceContext traceContext;

    @BeforeEach
    void setUp() {
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test event")
                .build();
        eventContext = new EventContext(event);
        traceContext = TraceContext.getOrCreate(eventContext, "exception-test");
        TraceContext.setThreadContext(traceContext);
    }

    @Test
    void testLogsCollected_whenActionThrowsException() {
        // Arrange
        traceContext.startLogCapture();
        
        // Simulate logging before exception
        LogEntry logBefore = createLogEntry("Before exception", "INFO");
        TraceContext.recordLog(logBefore);

        // Act - simulate exception scenario with error log
        LogEntry logDuringError = createLogEntry("RuntimeException: Something went wrong", "ERROR");
        TraceContext.recordLog(logDuringError);
        
        List<LogEntry> logs = traceContext.stopLogCapture();

        // Add action with logs (exception details captured in ERROR log)
        ActionTraceEntry action = ActionTraceEntry.builder()
                .type("failingAction")
                .alias("action1")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.addAction(action);

        // Assert
        ExecutionTrace result = traceContext.complete();
        assertThat(result.getTrace().getActions()).hasSize(1);
        
        // Verify logs remain in component entry (not aggregated to trace level)
        ActionTraceEntry actionEntry = result.getTrace().getActions().get(0);
        assertThat(actionEntry.getLogs()).hasSize(2);
        assertThat(actionEntry.getLogs()).anyMatch(log -> "ERROR".equals(log.getLevel()));
        assertThat(actionEntry.getLogs()).anyMatch(log -> 
            log.getFormattedMessage().contains("RuntimeException"));
        
        // Trace-level logs should be empty (logs stay in component)
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogsCollected_whenConditionThrowsException() {
        // Arrange
        traceContext.startLogCapture();
        
        LogEntry log1 = createLogEntry("Evaluating condition", "DEBUG");
        LogEntry log2 = createLogEntry("IllegalStateException: Invalid state", "ERROR");
        TraceContext.recordLog(log1);
        TraceContext.recordLog(log2);
        
        List<LogEntry> logs = traceContext.stopLogCapture();

        ConditionTraceEntry condition = ConditionTraceEntry.builder()
                .type("failingCondition")
                .satisfied(false)
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.addCondition(condition);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Logs remain in condition entry
        assertThat(result.getTrace().getConditions()).hasSize(1);
        ConditionTraceEntry conditionEntry = result.getTrace().getConditions().get(0);
        assertThat(conditionEntry.getLogs()).hasSize(2);
        assertThat(conditionEntry.getLogs()).anyMatch(log -> 
            log.getFormattedMessage().contains("IllegalStateException"));
        
        // Trace-level logs should be empty
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogsCollected_whenVariableResolutionFails() {
        // Arrange
        traceContext.startLogCapture();
        
        LogEntry log1 = createLogEntry("Resolving variable", "TRACE");
        LogEntry log2 = createLogEntry("VariableNotFoundException: Variable 'missing' not found", "WARN");
        TraceContext.recordLog(log1);
        TraceContext.recordLog(log2);
        
        List<LogEntry> logs = traceContext.stopLogCapture();

        VariableTraceEntry variable = VariableTraceEntry.builder()
                .type("expression")
                .alias("myVar")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.addVariable(variable);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Logs remain in variable entry
        assertThat(result.getTrace().getVariables()).hasSize(1);
        VariableTraceEntry varEntry = result.getTrace().getVariables().get(0);
        assertThat(varEntry.getLogs()).hasSize(2);
        assertThat(varEntry.getLogs()).anyMatch(log -> 
            log.getFormattedMessage().contains("VariableNotFoundException"));
        
        // Trace-level logs should be empty
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogsCollected_withNestedExceptions() {
        // Arrange
        traceContext.startLogCapture();
        LogEntry parentLog = createLogEntry("Parent action started", "INFO");
        TraceContext.recordLog(parentLog);
        List<LogEntry> parentLogs = traceContext.stopLogCapture();

        // Enter nested scope
        TraceChildren nested = traceContext.enterNestedScope();
        
        traceContext.startLogCapture();
        LogEntry nestedLog1 = createLogEntry("Nested action processing", "DEBUG");
        LogEntry nestedLog2 = createLogEntry("NullPointerException: Null value encountered", "ERROR");
        TraceContext.recordLog(nestedLog1);
        TraceContext.recordLog(nestedLog2);
        List<LogEntry> nestedLogs = traceContext.stopLogCapture();

        ActionTraceEntry nestedAction = ActionTraceEntry.builder()
                .type("nestedAction")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(nestedLogs)
                .build();
        nested.getActions().add(nestedAction);

        traceContext.exitNestedScope();

        // Add parent action with nested children
        ActionTraceEntry parentAction = ActionTraceEntry.builder()
                .type("parentAction")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(parentLogs)
                .children(nested)
                .build();

        traceContext.addAction(parentAction);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Logs remain in their component hierarchy
        assertThat(result.getTrace().getActions()).hasSize(1);
        ActionTraceEntry parent = result.getTrace().getActions().get(0);
        assertThat(parent.getLogs()).hasSize(1);
        assertThat(parent.getChildren()).isNotNull();
        assertThat(parent.getChildren().getActions()).hasSize(1);
        
        ActionTraceEntry nestedActionEntry = parent.getChildren().getActions().get(0);
        assertThat(nestedActionEntry.getLogs()).hasSize(2);
        assertThat(nestedActionEntry.getLogs()).anyMatch(log -> 
            log.getFormattedMessage().contains("NullPointerException"));
        
        // Trace-level logs should be empty (logs stay in component hierarchy)
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogsCollected_multipleExceptionsInSequence() {
        // Arrange - First failing action
        traceContext.startLogCapture();
        LogEntry log1 = createLogEntry("Action 1 started", "INFO");
        LogEntry log2 = createLogEntry("Exception in action 1", "ERROR");
        TraceContext.recordLog(log1);
        TraceContext.recordLog(log2);
        List<LogEntry> logs1 = traceContext.stopLogCapture();

        ActionTraceEntry action1 = ActionTraceEntry.builder()
                .type("action1")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs1)
                .build();
        traceContext.addAction(action1);

        // Second failing action
        traceContext.startLogCapture();
        LogEntry log3 = createLogEntry("Action 2 started", "INFO");
        LogEntry log4 = createLogEntry("Exception in action 2", "ERROR");
        TraceContext.recordLog(log3);
        TraceContext.recordLog(log4);
        List<LogEntry> logs2 = traceContext.stopLogCapture();

        ActionTraceEntry action2 = ActionTraceEntry.builder()
                .type("action2")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs2)
                .build();
        traceContext.addAction(action2);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - Logs remain in their action entries despite exceptions
        assertThat(result.getTrace().getActions()).hasSize(2);
        
        ActionTraceEntry action1Entry = result.getTrace().getActions().get(0);
        ActionTraceEntry action2Entry = result.getTrace().getActions().get(1);
        
        assertThat(action1Entry.getLogs()).hasSize(2);
        assertThat(action2Entry.getLogs()).hasSize(2);
        
        assertThat(action1Entry.getLogs()).filteredOn(log -> "ERROR".equals(log.getLevel())).hasSize(1);
        assertThat(action2Entry.getLogs()).filteredOn(log -> "ERROR".equals(log.getLevel())).hasSize(1);
        
        // Trace-level logs should be empty
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogsCollected_whenResultProcessingFails() {
        // Arrange
        traceContext.startLogCapture();
        LogEntry log1 = createLogEntry("Processing result", "INFO");
        LogEntry log2 = createLogEntry("ResultException: Failed to process result", "ERROR");
        TraceContext.recordLog(log1);
        TraceContext.recordLog(log2);
        List<LogEntry> logs = traceContext.stopLogCapture();

        ResultTraceEntry result = ResultTraceEntry.builder()
                .result("failed result")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.setResult(result);

        // Act
        ExecutionTrace executionTrace = traceContext.complete();

        // Assert - Logs remain in result entry
        assertThat(executionTrace.getTrace().getResult()).isNotNull();
        ResultTraceEntry resultEntry = executionTrace.getTrace().getResult();
        assertThat(resultEntry.getLogs()).hasSize(2);
        assertThat(resultEntry.getLogs()).anyMatch(log -> 
            log.getFormattedMessage().contains("ResultException"));
        
        // Trace-level logs should be empty
        assertThat(executionTrace.getLogs()).isEmpty();
    }

    @Test
    void testLogBufferStack_handlesExceptionsDuringNesting() {
        // Arrange - start outer capture first
        traceContext.startLogCapture();
        LogEntry log1 = createLogEntry("Outer scope log", "INFO");
        TraceContext.recordLog(log1);

        // Start nested capture
        traceContext.startLogCapture();
        LogEntry log2 = createLogEntry("Inner scope log", "DEBUG");
        TraceContext.recordLog(log2);

        // Simulate exception - stop inner capture
        List<LogEntry> innerLogs = traceContext.stopLogCapture();

        // Outer scope should still have its log
        List<LogEntry> outerLogs = traceContext.stopLogCapture();

        // Assert
        assertThat(innerLogs).hasSize(1);
        assertThat(innerLogs.get(0).getFormattedMessage()).isEqualTo("Inner scope log");
        assertThat(outerLogs).hasSize(1);
        assertThat(outerLogs.get(0).getFormattedMessage()).isEqualTo("Outer scope log");
    }

    @Test
    void testComplete_doesNotFail_whenLogsAreNull() {
        // Arrange - Add entries without logs
        ActionTraceEntry action = ActionTraceEntry.builder()
                .type("action")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(null)
                .build();
        traceContext.addAction(action);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert - should not throw exception
        assertThat(result).isNotNull();
        assertThat(result.getLogs()).isEmpty();
    }

    private LogEntry createLogEntry(String message, String level) {
        return LogEntry.builder()
                .message(message)
                .formattedMessage(message)
                .timestamp(Instant.now())
                .level(level)
                .build();
    }
}
