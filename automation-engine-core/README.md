# automation-engine-core

The core module of AutomationEngine provides the fundamental automation logic with **zero Spring dependencies**. This
makes it usable in any Java environment, not just Spring applications.

## Overview

This module contains the core abstractions and implementations for:

- **Automation definitions** - Immutable automation configurations
- **Automation execution** - Orchestration and execution flow
- **Component interfaces** - Triggers, conditions, actions, variables, and results
- **Event handling** - Event context and event propagation
- **Automation factory** - Parsing YAML/JSON/programmatic automation definitions

## Key Components

### 1. Automation

`com.davidrandoll.automation.engine.core.Automation`

The central class representing an immutable automation configuration.

**Structure:**

```java
public final class Automation {
    private final String alias;
    private final BaseVariableList variables;
    private final BaseTriggerList triggers;
    private final BaseConditionList conditions;
    private final BaseActionList actions;
    private final IBaseResult result;
}
```

**Execution Flow:**

1. `resolveVariables(EventContext)` - Resolve all variables in the context
2. `anyTriggerActivated(EventContext)` - Check if any trigger is activated
3. `allConditionsMet(EventContext)` - Verify all conditions are satisfied
4. `performActions(EventContext)` - Execute all actions sequentially
5. `getExecutionSummary(EventContext)` - Retrieve result/summary

**Example:**

```java
Automation automation = new Automation(
    "my-automation",
    variables,
    triggers,
    conditions,
    actions,
    resultProcessor
);

EventContext context = EventContext.of(myEvent);

// Execution
automation.resolveVariables(context);
if (automation.anyTriggerActivated(context) && automation.allConditionsMet(context)) {
    automation.performActions(context);
    Object result = automation.getExecutionSummary(context);
}
```

### 2. AutomationEngine

`com.davidrandoll.automation.engine.AutomationEngine`

Main API facade for registering and executing automations.

**Key Methods:**

```java
public interface AutomationEngine {
    // Registration
    void register(Automation automation);
    void unregister(String alias);
    Automation getAutomation(String alias);
    List<Automation> getAllAutomations();

    // Execution
    void publishEvent(IEvent event);
    void publishEvent(EventContext eventContext);
    Optional<Object> executeAutomation(Automation automation, EventContext context);
    Optional<Object> executeAutomationWithYaml(String yaml, IEvent event);
    Optional<Object> executeAutomationWithJson(String json, IEvent event);

    // Utilities
    EventFactory getEventFactory();
}
```

**Usage:**

```java
// Register
AutomationEngine engine = ...;
Automation automation = ...;
engine.register(automation);

// Publish event (triggers matching automations)
MyEvent event = new MyEvent("data");
engine.publishEvent(event);

// Execute specific automation
EventContext context = EventContext.of(event);
Optional<Object> result = engine.executeAutomation(automation, context);
```

### 3. AutomationOrchestrator

`com.davidrandoll.automation.engine.orchestrator.AutomationOrchestrator`

Manages the collection of registered automations and orchestrates execution.

**Features:**

- Thread-safe automation registry using `CopyOnWriteArrayList`
- Event publishing to matching automations
- Lifecycle event publishing (`AutomationEngineRegisterEvent`, `AutomationEngineProcessedEvent`)
- Interceptor chain pattern for extensibility

**Interceptors:**

```java
public interface IAutomationExecutionInterceptor {
    void beforeExecution(EventContext eventContext, Automation automation);
    void afterExecution(EventContext eventContext, Automation automation, Object result);
    void onException(EventContext eventContext, Automation automation, Exception exception);
}

public interface IAutomationHandleEventInterceptor {
    void beforeHandleEvent(EventContext eventContext);
    void afterHandleEvent(EventContext eventContext);
}
```

### 4. AutomationFactory

`com.davidrandoll.automation.engine.creator.AutomationFactory`

Factory for creating `Automation` instances from various formats.

**Supported Formats:**

- **YAML** - `YamlAutomationParser`
- **JSON** - `JsonAutomationParser`
- **Programmatic** - `ManualAutomationBuilder`

**Usage:**

```java
AutomationFactory factory = ...;

// From YAML
String yaml = """
    alias: my-automation
    triggers:
      - trigger: onEvent
        eventName: MyEvent
    actions:
      - action: logger
        message: "Event received"
    """;
Automation automation = factory.createAutomation("yaml", yaml);

// From JSON
String json = "{\"alias\":\"my-automation\",\"triggers\":[...]}";
Automation automation = factory.createAutomation("json", json);
```

### 5. EventContext

`com.davidrandoll.automation.engine.core.events.EventContext`

Wraps an event with metadata and variables for execution context.

**Structure:**

```java
public class EventContext {
    private final IEvent event;
    private final Map<String, Object> metadata; // Mutable during execution

    public static EventContext of(IEvent event);
    public void addMetadata(String key, Object value);
    public Object getMetadata(String key);
}
```

**Usage:**

```java
MyEvent event = new MyEvent("data");
EventContext context = EventContext.of(event);

// Actions can store data
context.addMetadata("result", someValue);

// Later retrieve
Object result = context.getMetadata("result");
```

## Core Interfaces

### Triggers

```java
@FunctionalInterface
public interface IBaseTrigger {
    boolean isTriggered(EventContext eventContext);
}
```

Triggers determine if an automation should execute based on the event.

### Conditions

```java
@FunctionalInterface
public interface IBaseCondition {
    boolean isSatisfied(EventContext eventContext);
}
```

Conditions are additional checks that must pass before actions execute.

### Actions

```java
@FunctionalInterface
public interface IBaseAction {
    void execute(EventContext eventContext);
}
```

Actions perform the actual work of the automation.

### Variables

```java
@FunctionalInterface
public interface IBaseVariable {
    void resolve(EventContext eventContext);
}
```

Variables compute and store values in the event context.

### Results

```java
@FunctionalInterface
public interface IBaseResult {
    Object getResult(EventContext eventContext);
}
```

Results extract a return value from the execution context.

## AutomationProcessor

`com.davidrandoll.automation.engine.creator.AutomationProcessor`

Central utility for processing automation components.

**Key Methods:**

```java
public class AutomationProcessor {
    // Actions
    BaseActionList resolveActions(List<ActionDefinition> actions);
    void executeActions(EventContext ec, List<ActionDefinition> actions);
    void executeActionsAsync(EventContext ec, List<ActionDefinition> actions);

    // Conditions
    BaseConditionList resolveConditions(List<ConditionDefinition> conditions);
    boolean allConditionsSatisfied(EventContext ec, List<ConditionDefinition> conditions);
    boolean anyConditionSatisfied(EventContext ec, List<ConditionDefinition> conditions);

    // Triggers
    BaseTriggerList resolveTriggers(List<TriggerDefinition> triggers);
    boolean anyTriggersTriggered(EventContext ec, List<TriggerDefinition> triggers);
    boolean allTriggersTriggered(EventContext ec, List<TriggerDefinition> triggers);

    // Variables
    BaseVariableList resolveVariables(List<VariableDefinition> variables);
    void resolveVariables(EventContext ec, List<VariableDefinition> variables);

    // Results
    IBaseResult resolveResult(ResultDefinition result);

    // Expressions
    String resolveExpression(EventContext ec, String expression);
}
```

## Thread Safety

The core module is designed for thread-safe operation:

- ✅ **Automation** - Immutable (all fields are final)
- ✅ **AutomationOrchestrator** - Uses `CopyOnWriteArrayList` for registration
- ✅ **EventContext** - Allows mutation but typically used in single-threaded execution
- ✅ **Component Lists** - `ArrayList` subclasses (not thread-safe but typically built once)

## Dependencies

This module has **NO Spring dependencies**. It only depends on:

- `com.fasterxml.jackson.core:jackson-databind` - JSON processing
- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` - YAML parsing
- `org.projectlombok:lombok` - Boilerplate reduction
- `org.slf4j:slf4j-api` - Logging abstraction
- `org.apache.commons:commons-lang3` - Utility functions

## Usage Without Spring

```java
// 1. Create suppliers for components
ITriggerSupplier triggerSupplier = new MapBasedTriggerSupplier(
    Map.of("alwaysTrue", new AlwaysTrueTrigger())
);

IActionSupplier actionSupplier = new MapBasedActionSupplier(
    Map.of("logger", new LoggerAction())
);

// 2. Create builders
TriggerBuilder triggerBuilder = new TriggerBuilder(triggerSupplier, List.of());
ActionBuilder actionBuilder = new ActionBuilder(actionSupplier, List.of());
// ... similar for conditions, variables, results

// 3. Create processor
AutomationProcessor processor = new AutomationProcessor(
    actionBuilder,
    conditionBuilder,
    triggerBuilder,
    variableBuilder,
    resultBuilder
);

// 4. Create factory
AutomationFactory factory = new AutomationFactory(processor);

// 5. Create orchestrator
AutomationOrchestrator orchestrator = new AutomationOrchestrator(
    List.of(), // execution interceptors
    List.of()  // handle event interceptors
);

// 6. Create engine
AutomationEngine engine = new IAEEngine(orchestrator, factory);

// 7. Use it!
String yaml = "...";
Automation automation = factory.createAutomation("yaml", yaml);
engine.register(automation);
engine.publishEvent(myEvent);
```

## Testing

The core module includes comprehensive unit tests with high coverage:

```java
@Test
void testAutomationExecution() {
    // Create components
    IBaseTrigger trigger = ec -> true;
    IBaseAction action = ec -> ec.addMetadata("executed", true);

    // Build automation
    Automation automation = new Automation(
        "test",
        BaseVariableList.of(),
        BaseTriggerList.of(trigger),
        BaseConditionList.of(),
        BaseActionList.of(action),
        ec -> ec.getMetadata("executed")
    );

    // Execute
    EventContext context = EventContext.of(new GenericEvent());
    automation.resolveVariables(context);
    assertTrue(automation.anyTriggerActivated(context));
    automation.performActions(context);

    // Verify
    assertEquals(true, automation.getExecutionSummary(context));
}
```

## See Also

- **[automation-engine-spring](../automation-engine-spring/README.md)** - Spring integration layer
- **[automation-engine-templating](../automation-engine-templating/README.md)** - Template engine for expressions
- **[Main README](../README.md)** - Project overview and getting started guide
