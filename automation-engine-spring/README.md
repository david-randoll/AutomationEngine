# automation-engine-spring

The Spring integration layer providing the Service Provider Interface (SPI) for extending AutomationEngine with custom
components.

## Overview

This module bridges the gap between the Spring-agnostic `automation-engine-core` and Spring-based applications. It
provides:

- **Pluggable Base Classes** - `PluggableAction`, `PluggableCondition`, `PluggableTrigger`
- **Spring Integration** - Dependency injection, AOP support, bean lifecycle management
- **Component Discovery** - Automatic discovery of custom components via Spring context
- **User-Defined Features** - Reusable, parameterized actions, conditions, and triggers
- **Built-in Components** - Common triggers, actions, and conditions ready to use

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│            Your Custom Components                       │
│   (Extend Pluggable* base classes)                     │
├─────────────────────────────────────────────────────────┤
│         automation-engine-spring (This Module)          │
│                                                         │
│  SPI Layer:                                            │
│  ├── PluggableAction<T>                                │
│  ├── PluggableCondition<T>                             │
│  ├── PluggableTrigger<T>                               │
│  ├── PluggableVariable<T>                              │
│  └── PluggableResult<T>                                │
│                                                         │
│  Component Suppliers:                                  │
│  ├── SpringActionSupplier                              │
│  ├── SpringConditionSupplier                           │
│  ├── SpringTriggerSupplier                             │
│  └── (Discovers beans from Spring context)            │
│                                                         │
│  Built-in Components:                                  │
│  ├── AlwaysTrueCondition, AlwaysFalseCondition        │
│  ├── TemplateCondition, TemplateAction                │
│  ├── UserDefinedAction, UserDefinedCondition          │
│  └── LoggerAction, StopAction, etc.                   │
├─────────────────────────────────────────────────────────┤
│           automation-engine-templating                  │
├─────────────────────────────────────────────────────────┤
│             automation-engine-core                      │
└─────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Pluggable Base Classes

#### PluggableAction<T>

`com.davidrandoll.automation.engine.spring.spi.PluggableAction`

Base class for all custom actions in Spring environments.

**Features:**

- ✅ Automatic injection of `AutomationProcessor`, `ITypeConverter`, `ApplicationContext`
- ✅ Access to Spring AOP proxy via `getSelf()`
- ✅ Delegated `AutomationProcessor` methods (e.g., `resolveExpression()`)
- ✅ Type-safe context class mapping

**Example:**

```java
@Component("sendEmail")
@Slf4j
public class SendEmailAction extends PluggableAction<SendEmailContext> {

    @Autowired
    private EmailService emailService;  // Inject any Spring bean

    @Override
    public void doExecute(EventContext ec, SendEmailContext ctx) {
        String to = ctx.getTo();
        String subject = ctx.getSubject();
        String body = ctx.getBody();

        log.info("Sending email to: {}", to);
        emailService.send(to, subject, body);
    }
}

@Data
public class SendEmailContext implements IActionContext {
    private String alias;
    private String description;
    private String to;
    private String subject;
    private String body;
}
```

**YAML Usage:**

```yaml
actions:
  - action: sendEmail # Matches @Component name
    to: "{{ event.user.email }}"
    subject: "Welcome!"
    body: "Hello {{ event.user.name }}"
```

#### PluggableCondition<T>

`com.davidrandoll.automation.engine.spring.spi.PluggableCondition`

Base class for custom conditions.

**Example:**

```java
@Component("isBusinessHours")
public class IsBusinessHoursCondition extends PluggableCondition<EmptyConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, EmptyConditionContext ctx) {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);

        return !now.isBefore(start) && !now.isAfter(end);
    }
}
```

**YAML Usage:**

```yaml
conditions:
  - condition: isBusinessHours
```

#### PluggableTrigger<T>

`com.davidrandoll.automation.engine.spring.spi.PluggableTrigger`

Base class for custom triggers.

**Example:**

```java
@Component("onEvent")
public class OnEventTrigger extends PluggableTrigger<OnEventTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, OnEventTriggerContext ctx) {
        String eventName = ec.getEvent().getClass().getSimpleName();
        return eventName.equals(ctx.getEventName());
    }
}

@Data
public class OnEventTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private String eventName;
}
```

**YAML Usage:**

```yaml
triggers:
  - trigger: onEvent
    eventName: UserCreatedEvent
```

### 2. Component Suppliers

Component suppliers discover and provide components from the Spring context.

**SpringActionSupplier**

```java
@Component
public class SpringActionSupplier implements IActionSupplier {

    @Autowired
    private ApplicationContext context;

    @Override
    public IAction getAction(String name) {
        return context.getBean(name, IAction.class);
    }
}
```

This allows AutomationEngine to find actions registered as `@Component` beans.

### 3. Type Converter

`com.davidrandoll.automation.engine.spring.spi.ITypeConverter`

Handles conversion between automation definitions and typed context objects.

**Implementation:**

```java
@Component
public class JacksonTypeConverter implements ITypeConverter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public <T> T convert(Map<String, Object> source, Class<T> targetType) {
        return objectMapper.convertValue(source, targetType);
    }
}
```

This enables automatic mapping of YAML/JSON properties to context class fields.

## Built-in Components

### Actions

| Action                            | Description                     | Context Class                 |
| --------------------------------- | ------------------------------- | ----------------------------- |
| `logger`                          | Logs a message                  | `LoggerActionContext`         |
| `stop`                            | Stops automation execution      | `EmptyActionContext`          |
| `setVariable`                     | Sets a variable value           | `SetVariableActionContext`    |
| `waitForTrigger`                  | Waits for a trigger to activate | `WaitForTriggerActionContext` |
| `userDefinedAction` / `udaAction` | Executes a user-defined action  | `UserDefinedActionContext`    |

### Conditions

| Condition                               | Description                          | Context Class                 |
| --------------------------------------- | ------------------------------------ | ----------------------------- |
| `alwaysTrue`                            | Always returns true                  | `EmptyConditionContext`       |
| `alwaysFalse`                           | Always returns false                 | `EmptyConditionContext`       |
| `template`                              | Evaluates Pebble template expression | `TemplateConditionContext`    |
| `userDefinedCondition` / `udcCondition` | Evaluates user-defined condition     | `UserDefinedConditionContext` |

### Triggers

| Trigger                             | Description                       | Context Class               |
| ----------------------------------- | --------------------------------- | --------------------------- |
| `alwaysTrue`                        | Always triggers                   | `AlwaysTrueTriggerContext`  |
| `alwaysFalse`                       | Never triggers                    | `AlwaysFalseTriggerContext` |
| `template`                          | Triggers when expression is true  | `TemplateTriggerContext`    |
| `userDefinedTrigger` / `udtTrigger` | Triggers based on user definition | `UserDefinedTriggerContext` |

## User-Defined Features

The module supports creating reusable, parameterized components:

### User-Defined Actions (UDA)

Define an action once, use it many times with different parameters.

**Definition:**

```yaml
name: sendNotification
actions:
  - action: sendEmail
    to: "{{ recipient }}"
    subject: "{{ subject }}"
    body: "{{ body }}"
```

**Usage:**

```yaml
actions:
  - action: uda
    name: sendNotification
    recipient: "admin@example.com"
    subject: "Alert"
    body: "High CPU usage detected"
```

### Registries

User-defined features are managed by registries:

```java
public interface IUserDefinedActionRegistry {
    void register(String name, UserDefinedActionDefinition definition);
    UserDefinedActionDefinition get(String name);
    Map<String, UserDefinedActionDefinition> getAll();
    void unregister(String name);
}
```

Similar interfaces exist for:

- `IUserDefinedConditionRegistry`
- `IUserDefinedTriggerRegistry`
- `IUserDefinedVariableRegistry`

## Advanced Features

### Using AutomationProcessor in Custom Components

All `Pluggable*` base classes expose `AutomationProcessor` via `@Delegate`:

```java
@Component("complexAction")
public class ComplexAction extends PluggableAction<ComplexActionContext> {

    @Override
    public void doExecute(EventContext ec, ComplexActionContext ctx) {
        // Resolve expressions
        String value = resolveExpression(ec, "{{ event.user.email }}");

        // Execute sub-actions dynamically
        executeActions(ec, List.of(
            ActionDefinition.builder()
                .action("logger")
                .param("message", "Processing: " + value)
                .build()
        ));

        // Check conditions
        boolean shouldContinue = allConditionsSatisfied(ec, ctx.getConditions());

        // Evaluate triggers
        boolean triggered = anyTriggersTriggered(ec, ctx.getTriggers());
    }
}
```

### Interceptors

Add cross-cutting concerns by implementing interceptor interfaces:

```java
@Component
public class MetricsActionInterceptor implements IActionInterceptor {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public void beforeExecution(EventContext ec, IActionContext ctx) {
        meterRegistry.counter("automation.action.execution",
            "action", ctx.getAlias()).increment();
    }

    @Override
    public void afterExecution(EventContext ec, IActionContext ctx) {
        // Record execution time, etc.
    }

    @Override
    public void onException(EventContext ec, IActionContext ctx, Exception ex) {
        meterRegistry.counter("automation.action.error",
            "action", ctx.getAlias()).increment();
    }
}
```

Interceptors are automatically discovered and applied to all components.

## Configuration

### Auto-Configuration

This module provides Spring Boot auto-configuration:

**ModulesConfig.java** - Registers all built-in components **BuilderConfig.java** - Configures builders and processors
**SupplierConfig.java** - Sets up component suppliers

### Custom Configuration

Override default beans:

```java
@Configuration
public class CustomAutomationConfig {

    @Bean
    @ConditionalOnMissingBean
    public ITypeConverter customTypeConverter() {
        return new MyCustomTypeConverter();
    }

    @Bean
    public IActionInterceptor customInterceptor() {
        return new MyCustomInterceptor();
    }
}
```

## Testing

Use the provided base test class:

```java
@SpringBootTest
class MyCustomActionTest extends AutomationEngineTest {

    @Test
    void testCustomAction() {
        var yaml = """
            alias: test-automation
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: myCustomAction
                param1: value1
            """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new CustomEvent();
        engine.publishEvent(event);

        // Assertions...
    }
}
```

## Dependencies

- `automation-engine-core` - Core automation logic
- `automation-engine-templating` - Template engine
- `org.springframework.boot:spring-boot-starter` - Spring Boot support
- `com.fasterxml.jackson.core:jackson-databind` - JSON processing

## See Also

- **[USER_DEFINED_FEATURES.md](../USER_DEFINED_FEATURES.md)** - Complete guide to user-defined features
- **[automation-engine-core](../automation-engine-core/README.md)** - Core concepts
- **[Examples](../examples/todo-example/)** - Working examples with custom components
