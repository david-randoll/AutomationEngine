package com.davidrandoll.automation.engine.tracing;

import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for log aggregation in TraceContext.
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
    void testLogAggregation_fromVariables() {
        // Arrange
        List<LogEntry> logs1 = List.of(
                createLog("Variable 1 log 1", "INFO"),
                createLog("Variable 1 log 2", "DEBUG")
        );
        List<LogEntry> logs2 = List.of(
                createLog("Variable 2 log", "TRACE")
        );

        VariableTraceEntry var1 = createVariableEntry("var1", logs1);
        VariableTraceEntry var2 = createVariableEntry("var2", logs2);

        traceContext.addVariable(var1);
        traceContext.addVariable(var2);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).containsAll(logs1);
        assertThat(result.getLogs()).containsAll(logs2);
    }

    @Test
    void testLogAggregation_fromTriggers() {
        // Arrange
        List<LogEntry> logs = List.of(
                createLog("Trigger check", "DEBUG"),
                createLog("Trigger activated", "INFO")
        );

        TriggerTraceEntry trigger = TriggerTraceEntry.builder()
                .type("trigger")
                .activated(true)
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.addTrigger(trigger);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).containsAll(logs);
    }

    @Test
    void testLogAggregation_fromConditions() {
        // Arrange
        List<LogEntry> logs = List.of(
                createLog("Evaluating condition", "DEBUG"),
                createLog("Condition satisfied", "INFO")
        );

        ConditionTraceEntry condition = ConditionTraceEntry.builder()
                .type("condition")
                .satisfied(true)
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.addCondition(condition);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).containsAll(logs);
    }

    @Test
    void testLogAggregation_fromActions() {
        // Arrange
        List<LogEntry> logs = List.of(
                createLog("Action started", "INFO"),
                createLog("Action processing", "DEBUG"),
                createLog("Action completed", "INFO")
        );

        ActionTraceEntry action = ActionTraceEntry.builder()
                .type("action")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.addAction(action);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).containsAll(logs);
    }

    @Test
    void testLogAggregation_fromResult() {
        // Arrange
        List<LogEntry> logs = List.of(
                createLog("Processing result", "INFO"),
                createLog("Result formatted", "DEBUG")
        );

        ResultTraceEntry resultEntry = ResultTraceEntry.builder()
                .result("test result")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(logs)
                .build();

        traceContext.setResult(resultEntry);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).containsAll(logs);
    }

    @Test
    void testLogAggregation_fromAllComponentTypes() {
        // Arrange
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

        // Assert
        assertThat(result.getLogs()).hasSize(5);
        assertThat(result.getLogs()).containsAll(varLogs);
        assertThat(result.getLogs()).containsAll(triggerLogs);
        assertThat(result.getLogs()).containsAll(conditionLogs);
        assertThat(result.getLogs()).containsAll(actionLogs);
        assertThat(result.getLogs()).containsAll(resultLogs);
    }

    @Test
    void testLogAggregation_fromNestedChildren() {
        // Arrange
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

        // Assert
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).containsAll(parentLogs);
        assertThat(result.getLogs()).containsAll(childLogs);
    }

    @Test
    void testLogAggregation_deeplyNestedChildren() {
        // Arrange
        List<LogEntry> level1Logs = List.of(createLog("Level 1", "INFO"));
        List<LogEntry> level2Logs = List.of(createLog("Level 2", "DEBUG"));
        List<LogEntry> level3Logs = List.of(createLog("Level 3", "TRACE"));

        // Level 3 (deepest)
        ActionTraceEntry level3Action = ActionTraceEntry.builder()
                .type("level3")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(level3Logs)
                .build();

        TraceChildren level3Children = new TraceChildren();
        level3Children.getActions().add(level3Action);

        // Level 2
        ActionTraceEntry level2Action = ActionTraceEntry.builder()
                .type("level2")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(level2Logs)
                .children(level3Children)
                .build();

        TraceChildren level2Children = new TraceChildren();
        level2Children.getActions().add(level2Action);

        // Level 1 (root)
        ActionTraceEntry level1Action = ActionTraceEntry.builder()
                .type("level1")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(level1Logs)
                .children(level2Children)
                .build();

        traceContext.addAction(level1Action);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).containsAll(level1Logs);
        assertThat(result.getLogs()).containsAll(level2Logs);
        assertThat(result.getLogs()).containsAll(level3Logs);
    }

    @Test
    void testLogAggregation_multipleNestedBranches() {
        // Arrange
        List<LogEntry> parentLogs = List.of(createLog("Parent", "INFO"));
        List<LogEntry> branch1Logs = List.of(createLog("Branch 1", "DEBUG"));
        List<LogEntry> branch2Logs = List.of(createLog("Branch 2", "DEBUG"));

        // Branch 1
        ActionTraceEntry branch1Action = ActionTraceEntry.builder()
                .type("branch1")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(branch1Logs)
                .build();

        // Branch 2
        ActionTraceEntry branch2Action = ActionTraceEntry.builder()
                .type("branch2")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(branch2Logs)
                .build();

        TraceChildren children = new TraceChildren();
        children.getActions().add(branch1Action);
        children.getActions().add(branch2Action);

        ActionTraceEntry parentAction = ActionTraceEntry.builder()
                .type("parent")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(parentLogs)
                .children(children)
                .build();

        traceContext.addAction(parentAction);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(3);
        assertThat(result.getLogs()).containsAll(parentLogs);
        assertThat(result.getLogs()).containsAll(branch1Logs);
        assertThat(result.getLogs()).containsAll(branch2Logs);
    }

    @Test
    void testLogAggregation_handlesNullLogs() {
        // Arrange
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
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogAggregation_handlesEmptyLogs() {
        // Arrange
        ActionTraceEntry action = ActionTraceEntry.builder()
                .type("action")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(new ArrayList<>())
                .build();

        traceContext.addAction(action);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    void testLogAggregation_preservesOrder() {
        // Arrange
        List<LogEntry> logs1 = List.of(createLog("First", "INFO", 1000));
        List<LogEntry> logs2 = List.of(createLog("Second", "INFO", 2000));
        List<LogEntry> logs3 = List.of(createLog("Third", "INFO", 3000));

        traceContext.addAction(createActionEntry(logs1));
        traceContext.addAction(createActionEntry(logs2));
        traceContext.addAction(createActionEntry(logs3));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(3);
        // Verify order is preserved (first action's logs, then second, then third)
        assertThat(result.getLogs().get(0).getFormattedMessage()).isEqualTo("First");
        assertThat(result.getLogs().get(1).getFormattedMessage()).isEqualTo("Second");
        assertThat(result.getLogs().get(2).getFormattedMessage()).isEqualTo("Third");
    }

    @Test
    void testLogAggregation_withAllLogLevels() {
        // Arrange
        List<LogEntry> logs = List.of(
                createLog("Error message", "ERROR"),
                createLog("Warning message", "WARN"),
                createLog("Info message", "INFO"),
                createLog("Debug message", "DEBUG"),
                createLog("Trace message", "TRACE")
        );

        traceContext.addAction(createActionEntry(logs));

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(5);
        assertThat(result.getLogs()).filteredOn(log -> "ERROR".equals(log.getLevel())).hasSize(1);
        assertThat(result.getLogs()).filteredOn(log -> "WARN".equals(log.getLevel())).hasSize(1);
        assertThat(result.getLogs()).filteredOn(log -> "INFO".equals(log.getLevel())).hasSize(1);
        assertThat(result.getLogs()).filteredOn(log -> "DEBUG".equals(log.getLevel())).hasSize(1);
        assertThat(result.getLogs()).filteredOn(log -> "TRACE".equals(log.getLevel())).hasSize(1);
    }

    @Test
    void testLogAggregation_fromNestedVariables() {
        // Arrange
        List<LogEntry> nestedLogs = List.of(createLog("Nested variable", "DEBUG"));
        
        VariableTraceEntry nestedVar = createVariableEntry("nestedVar", nestedLogs);
        
        TraceChildren children = new TraceChildren();
        children.getVariables().add(nestedVar);

        List<LogEntry> parentLogs = List.of(createLog("Parent variable", "INFO"));
        VariableTraceEntry parentVar = VariableTraceEntry.builder()
                .type("parent")
                .alias("parentVar")
                .startedAt(System.currentTimeMillis())
                .finishedAt(System.currentTimeMillis())
                .logs(parentLogs)
                .children(children)
                .build();

        traceContext.addVariable(parentVar);

        // Act
        ExecutionTrace result = traceContext.complete();

        // Assert
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getLogs()).containsAll(parentLogs);
        assertThat(result.getLogs()).containsAll(nestedLogs);
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
