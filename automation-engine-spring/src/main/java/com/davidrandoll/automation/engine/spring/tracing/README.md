# Automation Execution Tracing

Comprehensive tracing and introspection system for AutomationEngine that captures detailed execution information at every level of automation execution.

## Overview

The tracing system provides deep visibility into automation execution without requiring OpenTelemetry or external dependencies. It captures:

- **Timing**: Start time, end time, and duration for the entire automation and each component
- **Triggers**: Which triggers were evaluated and which activated
- **Conditions**: Which conditions were checked and their results
- **Actions**: Which actions executed, their duration, and any exceptions
- **Variables**: Variable resolution with before/after values
- **Results**: Result computation timing and type
- **Context Snapshots**: Metadata state before and after execution

## Architecture

### Multi-Level Interceptor Chain

Tracing uses the existing interceptor pattern at multiple levels:

1. **AutomationTracingInterceptor** (automation-level) - Orchestrates tracing, generates trace IDs
2. **VariableTracingInterceptor** (component-level) - Traces variable resolution
3. **TriggerTracingInterceptor** (component-level) - Traces trigger evaluation
4. **ConditionTracingInterceptor** (component-level) - Traces condition checking
5. **ActionTracingInterceptor** (component-level) - Traces action execution
6. **ResultTracingInterceptor** (component-level) - Traces result computation

### Data Storage

Trace data is stored in two locations:

1. **During Execution**: `EventContext.metadata` with `__trace_*` keys (thread-safe)
2. **After Execution**: `AutomationResult.additionalFields` map under the "trace" key

## Configuration

Enable tracing in `application.properties` or `application.yml`:

```properties
# Enable tracing (default: false)
automation-engine.tracing.enabled=true

# Include context snapshots (default: true)
automation-engine.tracing.include-context-snapshots=true

# Snapshot mode: NONE, KEYS_ONLY, FULL (default: KEYS_ONLY)
automation-engine.tracing.snapshot-mode=KEYS_ONLY

# Clear trace data from EventContext after extraction (default: true)
automation-engine.tracing.clear-after-extraction=true

# Enable trace persistence - requires IAutomationTraceRepository bean (default: false)
automation-engine.tracing.persistence-enabled=false
```

## Usage

### Basic Usage

```java
@Autowired
private AutomationEngine engine;

// Execute automation (tracing happens automatically when enabled)
AutomationResult result = engine.executeAutomationWithYaml(yaml, event);

// Extract trace data
Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);

// Print trace for debugging
String formatted = AutomationTracePrinter.prettyPrint(result);
System.out.println(formatted);
```

### Accessing Trace Information

```java
// Get trace ID
String traceId = AutomationTraceExtractor.getTraceId(result);

// Get execution duration
Double durationMs = AutomationTraceExtractor.getDurationMillis(result);

// Get action traces
List<Map<String, Object>> actions = AutomationTraceExtractor.getActions(result);

// Get condition traces
List<Map<String, Object>> conditions = AutomationTraceExtractor.getConditions(result);

// Get trigger traces
List<Map<String, Object>> triggers = AutomationTraceExtractor.getTriggers(result);

// Check for action exceptions
boolean hasExceptions = AutomationTraceExtractor.hasActionExceptions(result);
```

### Pretty Printing

```java
// Full trace output with formatting
String fullTrace = AutomationTracePrinter.prettyPrint(result);

// Compact one-line summary
String summary = AutomationTracePrinter.summary(result);
// Output: [Trace abc-123] my-automation (45.234ms) - Executed: true
```

## Trace Data Structure

The trace map contains:

```json
{
  "traceId": "uuid-string",
  "automationAlias": "my-automation",
  "executed": true,
  "timing": {
    "startNanos": 123456789,
    "endNanos": 123556789,
    "durationNanos": 100000,
    "durationMillis": 0.1
  },
  "variables": [
    {
      "alias": "myVar",
      "type": "StaticVariableContext",
      "beforeValue": null,
      "afterValue": "value",
      "startTimeNanos": 123456789,
      "endTimeNanos": 123456889,
      "durationNanos": 100
    }
  ],
  "triggers": [
    {
      "alias": "onHttpRequest",
      "type": "OnHttpRequestTriggerContext",
      "activated": true,
      "startTimeNanos": 123457000,
      "endTimeNanos": 123457200,
      "durationNanos": 200
    }
  ],
  "triggersActivated": 1,
  "conditions": [
    {
      "alias": "isAuthenticated",
      "type": "ExpressionConditionContext",
      "satisfied": true,
      "startTimeNanos": 123457300,
      "endTimeNanos": 123457500,
      "durationNanos": 200
    }
  ],
  "allConditionsSatisfied": true,
  "actions": [
    {
      "alias": "sendEmail",
      "type": "SendEmailActionContext",
      "startTimeNanos": 123458000,
      "endTimeNanos": 123468000,
      "durationNanos": 10000
    }
  ],
  "actionsWithExceptions": 0,
  "result": {
    "resultType": "java.util.HashMap",
    "resultValue": "2 entries",
    "startTimeNanos": 123469000,
    "endTimeNanos": 123469100,
    "durationNanos": 100
  },
  "contextSnapshotBefore": {
    "metadataKeys": ["eventType", "timestamp"],
    "eventType": "com.example.MyEvent",
    "timestamp": "2025-12-19T10:30:00Z",
    "source": "MyController"
  },
  "contextSnapshotAfter": {
    "metadataKeys": ["eventType", "timestamp", "myVar", "resultVar"],
    "eventType": "com.example.MyEvent",
    "timestamp": "2025-12-19T10:30:00Z",
    "source": "MyController"
  }
}
```

## Performance Considerations

### Overhead

- **Minimal**: ~50-200ns per component for timing capture
- **Variable**: Map operations and object allocation vary by JVM
- **Configurable**: Disable tracing in production if not needed

### Memory

- **Trace data accumulates** in EventContext during execution
- **Cleared automatically** after extraction (if `clearAfterExtraction=true`)
- **Snapshot verbosity** controlled by `snapshotMode` setting

### Thread Safety

- ✅ **Fully thread-safe**: EventContext uses `NullableConcurrentHashMap`
- ✅ **Parallel actions**: Multiple actions can execute concurrently
- ✅ **No race conditions**: All metadata writes are atomic

## Best Practices

1. **Enable in Development**: Always enable tracing during development and testing
2. **Disable in Production**: Disable tracing in production unless debugging
3. **Use KEYS_ONLY mode**: Reduces memory overhead while preserving context visibility
4. **Clear after extraction**: Prevents memory leaks in long-running contexts
5. **Log summaries**: Use `AutomationTracePrinter.summary()` for concise logging

## Example Output

```
╔═══════════════════════════════════════════════════════════════
║ AUTOMATION EXECUTION TRACE
╠═══════════════════════════════════════════════════════════════
║ Trace ID: 3f7b8c2a-9d4e-4b1a-8c3d-9f8e7d6c5b4a
║ Automation: process-order
║ Executed: true
║
║ ⏱ TIMING
║   Duration: 45.234 ms
║   Duration (nanos): 45234000
║
║ 📝 VARIABLES (2)
║   1. orderId
║      - Before: null
║      - After: 12345
║      - Duration: 0.123 ms
║   2. customerEmail
║      - Before: null
║      - After: user@example.com
║      - Duration: 0.089 ms
║
║ 🎯 TRIGGERS (1)
║   Activated: 1 of 1
║   ✓ 1. onOrderCreated (OnOrderCreatedTriggerContext)
║      - Activated: true
║      - Duration: 0.234 ms
║
║ ✅ CONDITIONS (2)
║   All Satisfied: Yes
║   ✓ 1. isValidOrder (ExpressionConditionContext)
║      - Satisfied: true
║      - Duration: 0.156 ms
║   ✓ 2. hasInventory (ExpressionConditionContext)
║      - Satisfied: true
║      - Duration: 0.189 ms
║
║ ⚡ ACTIONS (3)
║   Exceptions: 0
║   ✓ 1. reserveInventory (ReserveInventoryActionContext)
║      - Duration: 15.234 ms
║   ✓ 2. sendConfirmationEmail (SendEmailActionContext)
║      - Duration: 28.456 ms
║   ✓ 3. logOrder (LogMessageActionContext)
║      - Duration: 0.892 ms
║
║ 📊 RESULT
║      - Type: java.util.HashMap
║      - Value: 3 entries
║      - Duration: 0.067 ms
║
║ 📸 CONTEXT SNAPSHOTS
║   Before: Available
║   After: Available
╚═══════════════════════════════════════════════════════════════
```

## Extension Points

### Custom Trace Persistence

Implement `IAutomationTraceRepository` to persist traces:

```java
public interface IAutomationTraceRepository {
    void save(Map<String, Object> trace);
}

@Component
public class DatabaseTraceRepository implements IAutomationTraceRepository {
    @Override
    public void save(Map<String, Object> trace) {
        // Save to database
    }
}
```

Enable persistence:
```properties
automation-engine.tracing.persistence-enabled=true
```

### Custom Trace Processing

Create an interceptor that processes trace data:

```java
@Component
@Order(101) // Run after AutomationTracingInterceptor
public class CustomTraceProcessor implements IAutomationExecutionInterceptor {
    @Override
    public AutomationResult intercept(Automation automation, EventContext context, 
                                      IAutomationExecutionChain chain) {
        AutomationResult result = chain.proceed(automation, context);
        
        // Process trace data
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        // ... custom processing ...
        
        return result;
    }
}
```

## Troubleshooting

### No Trace Data

**Problem**: `AutomationTraceExtractor.extractTrace()` returns empty map

**Solutions**:
1. Check `automation-engine.tracing.enabled=true` in configuration
2. Verify `TracingConfig` is being scanned by Spring
3. Check logs for tracing interceptor registration

### Missing Component Traces

**Problem**: Trace has timing but missing triggers/conditions/actions

**Solutions**:
1. Verify component interceptors are registered (check for beans)
2. Check interceptor order (@Order should be 200 or higher)
3. Ensure tracing is enabled before automation execution starts

### Memory Issues

**Problem**: High memory usage with tracing enabled

**Solutions**:
1. Set `automation-engine.tracing.clear-after-extraction=true`
2. Use `automation-engine.tracing.snapshot-mode=KEYS_ONLY` or `NONE`
3. Disable `include-context-snapshots` if not needed

## Related Documentation

- [Interceptor Pattern](../README.md#interceptors)
- [EventContext](../../automation-engine-core/README.md#eventcontext)
- [AutomationResult](../../automation-engine-core/README.md#automationresult)
