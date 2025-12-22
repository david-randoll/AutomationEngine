# Automation Engine Tracing

This module provides tracing and debugging capabilities for the Automation Engine, allowing users to track and analyze
the execution of automations in detail.

## Features

- **Execution Tracing**: Capture detailed execution traces of automations, including triggers, conditions, actions,
  variables, and results
- **Nested Child Tracking**: Track nested execution hierarchies for complex automations
- **Before/After Snapshots**: Capture state before and after each component execution
- **Timing Information**: Record precise timing for each component execution
- **Interceptor-Based**: Non-invasive tracing using interceptors that can be easily enabled/disabled

## Usage

### Enabling Tracing

Tracing is enabled by creating a `TraceContext` for an event:

```java
EventContext eventContext = new EventContext(event);
TraceContext traceContext = TraceContext.getOrCreate(eventContext, "my-automation");
```

### Retrieving Trace Results

After automation execution completes, retrieve the trace:

```java
TraceResult traceResult = traceContext.complete();
ExecutionTrace trace = traceResult.getTrace();
```

### Trace Structure

The `ExecutionTrace` contains:

- **Variables**: List of variable resolutions with before/after snapshots
- **Triggers**: List of trigger evaluations with activation status
- **Conditions**: List of condition evaluations with satisfaction status
- **Actions**: List of action executions with timing information
- **Result**: Final result computation

Each entry includes:

- Type and alias
- Start and finish timestamps
- Before and after snapshots (event state and context data)
- Nested children (if any)

## Components

### Core Classes

- `TraceContext`: Main entry point for tracing, manages trace state
- `ExecutionTrace`: Container for the complete execution trace
- `TraceChildren`: Container for nested child traces
- `TraceSnapshot`: Captures state at a point in time

### Trace Entries

- `VariableTraceEntry`: Traces variable resolution
- `TriggerTraceEntry`: Traces trigger evaluation
- `ConditionTraceEntry`: Traces condition evaluation
- `ActionTraceEntry`: Traces action execution
- `ResultTraceEntry`: Traces result computation

### Interceptors

- `TracingExecutionInterceptor`: Traces overall automation execution
- `TracingVariableInterceptor`: Traces variable resolutions
- `TracingTriggerInterceptor`: Traces trigger evaluations
- `TracingConditionInterceptor`: Traces condition evaluations
- `TracingActionInterceptor`: Traces action executions
- `TracingResultInterceptor`: Traces result computations

## Integration

This module integrates seamlessly with automation-engine-core through the interceptor pattern. The tracing interceptors
can be registered with the `AutomationOrchestrator` to enable tracing for all automations.
