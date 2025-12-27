# Logging Enhancement - Structured Log Capture

## Overview

The tracing module now captures logs as structured objects with detailed information, and aggregates them at the trace level for comprehensive analysis.

## Features

### 1. Structured LogEntry Object

Each captured log is now a structured object containing:

- **message**: The log message template (before argument substitution)
- **arguments**: Array of arguments passed to the logger
- **formattedMessage**: The fully formatted log message
- **timestamp**: When the log was created (as `Instant`)

```java
@Data
@Builder
public class LogEntry {
    private String message;
    private Object[] arguments;
    private String formattedMessage;
    private Instant timestamp;
}
```

### 2. Component-Level Log Capture

Each component trace entry (Variable, Trigger, Condition, Action, Result) captures logs that occurred during its execution:

```java
ActionTraceEntry entry = ActionTraceEntry.builder()
    .type("sendEmail")
    .alias("notify-user")
    .logs(logs)  // List<LogEntry> captured during action execution
    .build();
```

### 3. Trace-Level Log Aggregation

The `ExecutionTrace` now includes an aggregated `logs` field containing ALL logs from the entire automation execution:

```java
ExecutionTrace trace = traceContext.complete();
// trace.logs contains all logs from all components
```

This allows you to:
- View all logs in chronological order
- See the complete log history for debugging
- Analyze logging patterns across the entire execution

## Implementation Details

### TracingAppender

The custom Logback appender captures full log details from `ILoggingEvent`:

```java
protected void append(ILoggingEvent event) {
    LogEntry logEntry = LogEntry.builder()
            .message(event.getMessage())
            .arguments(event.getArgumentArray())
            .formattedMessage(event.getFormattedMessage())
            .timestamp(Instant.ofEpochMilli(event.getTimeStamp()))
            .build();
    TraceContext.recordLog(logEntry);
}
```

### Recursive Log Collection

When `TraceContext.complete()` is called, it recursively traverses all trace entries and collects their logs:

```java
// Aggregate all logs from all trace entries
List<LogEntry> allLogs = new ArrayList<>();
collectLogsFromVariables(root.getVariables(), allLogs);
collectLogsFromTriggers(root.getTriggers(), allLogs);
collectLogsFromConditions(root.getConditions(), allLogs);
collectLogsFromActions(root.getActions(), allLogs);
if (root.getResult() != null && root.getResult().getLogs() != null) {
    allLogs.addAll(root.getResult().getLogs());
}
executionTrace.setLogs(allLogs);
```

## Example Usage

### Accessing Component-Specific Logs

```java
ExecutionTrace trace = traceContext.complete();

// Get logs from a specific action
for (ActionTraceEntry action : trace.getTrace().getActions()) {
    for (LogEntry log : action.getLogs()) {
        System.out.println(log.getFormattedMessage() + " at " + log.getTimestamp());
    }
}
```

### Accessing All Logs at Once

```java
ExecutionTrace trace = traceContext.complete();

// Get all logs from entire execution
for (LogEntry log : trace.getLogs()) {
    System.out.println("[" + log.getTimestamp() + "] " + log.getFormattedMessage());
    if (log.getArguments() != null) {
        System.out.println("  Arguments: " + Arrays.toString(log.getArguments()));
    }
}
```

### Analyzing Log Patterns

```java
ExecutionTrace trace = traceContext.complete();

// Find all ERROR level logs (if captured in message)
List<LogEntry> errors = trace.getLogs().stream()
    .filter(log -> log.getFormattedMessage().contains("ERROR"))
    .collect(Collectors.toList());

// Group logs by time period
Map<LocalDateTime, List<LogEntry>> logsByMinute = trace.getLogs().stream()
    .collect(Collectors.groupingBy(log -> 
        LocalDateTime.ofInstant(log.getTimestamp(), ZoneId.systemDefault())
                     .truncatedTo(ChronoUnit.MINUTES)
    ));
```

## Benefits

1. **Full Context**: The structured format captures both the template and final message, useful for debugging parameterized logs
2. **Temporal Analysis**: Timestamps allow sorting and filtering logs by time
3. **Hierarchical View**: Component-level logs for detailed analysis
4. **Holistic View**: Trace-level logs for seeing the complete picture
5. **Flexibility**: Structured data enables custom analysis and reporting

## Backward Compatibility

The log capture mechanism is fully backward compatible:
- Existing code continues to work unchanged
- LogEntry is a new type that extends functionality
- All trace entry types (Action, Condition, Trigger, Variable, Result) updated to use `List<LogEntry>`
