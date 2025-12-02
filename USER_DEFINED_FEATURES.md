# User-Defined Features

This document explains how to use the new user-defined features in the Automation Engine.

## Overview

The Automation Engine now supports user-defined actions, conditions, triggers, and variables. These allow you to create reusable, parameterized components similar to how `AutomationDefinition` works, but for individual building blocks.

## Features

### 1. User-Defined Actions

Create custom actions that can be reused across automations with parameters.

**Registry Interface**: `IUserDefinedActionRegistry`
**Default Implementation**: `DefaultUserDefinedActionRegistry`
**Action Class**: `UserDefinedAction`
**Context Class**: `UserDefinedActionContext`

**Example Usage**:

```java
// Register a user-defined action
@Autowired
DefaultUserDefinedActionRegistry actionRegistry;

UserDefinedActionDefinition myAction = UserDefinedActionDefinition.builder()
    .name("sendNotification")
    .description("Sends a notification with a message")
    .parameters(Map.of("message", "Default message", "recipient", "admin"))
    .variables(List.of(/* variable definitions */))
    .conditions(List.of(/* condition definitions */))
    .actions(List.of(/* action definitions */))
    .build();

actionRegistry.registerAction(myAction);
```

**Using in automation**:

```yaml
actions:
  - type: userDefinedAction
    name: sendNotification
    message: "Hello World!"
    recipient: "user@example.com"
```

### 2. User-Defined Conditions

Create custom conditions that can be reused across automations.

**Registry Interface**: `IUserDefinedConditionRegistry`
**Default Implementation**: `DefaultUserDefinedConditionRegistry`
**Condition Class**: `UserDefinedCondition`
**Context Class**: `UserDefinedConditionContext`

**Example**:

```java
UserDefinedConditionDefinition myCondition = UserDefinedConditionDefinition.builder()
    .name("isBusinessHours")
    .description("Checks if current time is within business hours")
    .parameters(Map.of("startHour", 9, "endHour", 17))
    .variables(List.of(/* variable definitions */))
    .conditions(List.of(/* condition definitions */))
    .build();

conditionRegistry.registerCondition(myCondition);
```

**Using in automation**:

```yaml
conditions:
  - type: userDefinedCondition
    name: isBusinessHours
    startHour: 8
    endHour: 18
```

### 3. User-Defined Triggers

Create custom triggers that can be reused across automations.

**Registry Interface**: `IUserDefinedTriggerRegistry`
**Default Implementation**: `DefaultUserDefinedTriggerRegistry`
**Trigger Class**: `UserDefinedTrigger`
**Context Class**: `UserDefinedTriggerContext`

**Example**:

```java
UserDefinedTriggerDefinition myTrigger = UserDefinedTriggerDefinition.builder()
    .name("dailyReport")
    .description("Triggers at a specific time daily")
    .parameters(Map.of("hour", 9, "minute", 0))
    .variables(List.of(/* variable definitions */))
    .triggers(List.of(/* trigger definitions */))
    .build();

triggerRegistry.registerTrigger(myTrigger);
```

**Using in automation**:

```yaml
triggers:
  - type: userDefinedTrigger
    name: dailyReport
    hour: 10
    minute: 30
```

### 4. User-Defined Variables

Create custom variables that can be reused across automations.

**Registry Interface**: `IUserDefinedVariableRegistry`
**Default Implementation**: `DefaultUserDefinedVariableRegistry`
**Variable Class**: `UserDefinedVariable`
**Context Class**: `UserDefinedVariableContext`

**Example**:

```java
UserDefinedVariableDefinition myVariable = UserDefinedVariableDefinition.builder()
    .name("loadConfig")
    .description("Loads configuration from a source")
    .parameters(Map.of("configSource", "default"))
    .variables(List.of(/* variable definitions */))
    .build();

variableRegistry.registerVariable(myVariable);
```

**Using in automation**:

```yaml
variables:
  - type: userDefinedVariable
    name: loadConfig
    configSource: "production"
```

## Custom Registry Implementation

You can provide your own registry implementation to load definitions from a database, file system, or any other source:

```java
@Bean
public IUserDefinedActionRegistry customActionRegistry() {
    return new MyDatabaseActionRegistry();
}
```

The Spring configuration uses `@ConditionalOnMissingBean`, so your custom implementation will take precedence.

## Parameter Passing

When a user-defined component is invoked, any parameters specified in the context are added to the event metadata. These parameters can then be used in templates within the component's definition.

```yaml
# Define a user-defined action with parameters
actions:
  - type: userDefinedAction
    name: myAction
    param1: "{{someVariable}}"
    param2: 123
```

Inside the action definition, `param1` and `param2` will be available in the event context.

## Directory Structure

```
automation-engine-spring/
├── modules/
│   ├── actions/
│   │   └── uda/
│   │       ├── UserDefinedAction.java
│   │       ├── UserDefinedActionContext.java
│   │       ├── UserDefinedActionDefinition.java
│   │       ├── IUserDefinedActionRegistry.java
│   │       └── DefaultUserDefinedActionRegistry.java
│   ├── conditions/
│   │   └── udc/
│   │       ├── UserDefinedCondition.java
│   │       ├── UserDefinedConditionContext.java
│   │       ├── UserDefinedConditionDefinition.java
│   │       ├── IUserDefinedConditionRegistry.java
│   │       └── DefaultUserDefinedConditionRegistry.java
│   ├── triggers/
│   │   └── udt/
│   │       ├── UserDefinedTrigger.java
│   │       ├── UserDefinedTriggerContext.java
│   │       ├── UserDefinedTriggerDefinition.java
│   │       ├── IUserDefinedTriggerRegistry.java
│   │       └── DefaultUserDefinedTriggerRegistry.java
│   └── variables/
│       └── udv/
│           ├── UserDefinedVariable.java
│           ├── UserDefinedVariableContext.java
│           ├── UserDefinedVariableDefinition.java
│           ├── IUserDefinedVariableRegistry.java
│           └── DefaultUserDefinedVariableRegistry.java
```

## Benefits

1. **Reusability**: Define once, use many times
2. **Parameterization**: Customize behavior with parameters
3. **Composition**: Build complex logic from simpler user-defined components
4. **Maintainability**: Update definitions in one place
5. **Extensibility**: Implement custom registries for dynamic loading

## Notes

- All user-defined components support variables, allowing you to resolve metadata before execution
- Conditions in user-defined actions and conditions are evaluated using `allConditionsSatisfied`
- Triggers in user-defined triggers are evaluated using `anyTriggersTriggered`
- Parameters are merged into the event context metadata for use in templates

