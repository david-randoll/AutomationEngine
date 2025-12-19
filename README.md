# AutomationEngine

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

AutomationEngine is a powerful, flexible, and extensible Java library for building event-driven automation workflows. It
enables developers to define and execute complex automations using YAML, JSON, or Java code. Automations respond to
incoming events and context, making it easy to implement dynamic business logic in a structured, declarative way.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Module Overview](#module-overview)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Extensibility](#extensibility)
- [Inspiration](#inspiration)
- [Contributing](#contributing)

## Overview

AutomationEngine is built for:

- **Declarative Automation**: Define automation logic through YAML/JSON configuration or Java code
- **Event-Driven Architecture**: Respond to events in real time with customizable triggers
- **Conditional Execution**: Execute actions based on dynamic conditions and business rules
- **Spring Boot Integration**: Seamless integration into Spring Boot applications with auto-configuration
- **Pluggable Components**: Modular architecture where triggers, conditions, actions, variables, and results are all
  extensible

## Architecture

AutomationEngine follows a modular, layered architecture designed for flexibility and extensibility:

```
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                       │
├─────────────────────────────────────────────────────────────────┤
│          spring-boot-starter-automation-engine                  │
│                  (Aggregates all modules)                       │
├──────────────────┬──────────────────┬──────────────────────────┤
│  Spring Web      │  Spring JDBC     │  Spring AMQP             │
│  (HTTP)          │  (Database)      │  (RabbitMQ)              │
├──────────────────┴──────────────────┴──────────────────────────┤
│              automation-engine-spring                           │
│         (Spring Integration & SPI Layer)                        │
│  - PluggableAction, PluggableCondition, PluggableTrigger       │
│  - Spring beans, dependency injection, AOP support             │
├─────────────────────────────────────────────────────────────────┤
│           automation-engine-templating                          │
│              (Pebble Template Engine)                           │
├─────────────────────────────────────────────────────────────────┤
│              automation-engine-core                             │
│          (Core Automation Logic - No Spring)                    │
│  - Automation, AutomationEngine, AutomationOrchestrator        │
│  - Triggers, Conditions, Actions, Variables, Results           │
└─────────────────────────────────────────────────────────────────┘
```

### Design Principles

1. **Spring-Agnostic Core**: The `automation-engine-core` module has no Spring dependencies, making it usable in any
   Java environment
2. **Pluggable SPI**: The `automation-engine-spring` module provides Service Provider Interfaces (SPI) for extending
   functionality
3. **Domain-Specific Modules**: HTTP, JDBC, AMQP, and Events modules provide domain-specific triggers and actions
4. **Immutable Automations**: Automation definitions are immutable for thread safety
5. **Interceptor Pattern**: Execution interceptors allow cross-cutting concerns like logging, metrics, and validation

## Module Overview

AutomationEngine consists of 11 modules, each serving a specific purpose:

### Core Modules

| Module                                                                     | Description                                                | Key Components                                              |
| -------------------------------------------------------------------------- | ---------------------------------------------------------- | ----------------------------------------------------------- |
| **[automation-engine-core](automation-engine-core/README.md)**             | Core automation logic with no Spring dependencies          | `Automation`, `AutomationEngine`, `AutomationOrchestrator`  |
| **[automation-engine-templating](automation-engine-templating/README.md)** | Pebble template engine integration for dynamic expressions | `TemplateProcessor`, expression evaluation                  |
| **[automation-engine-spring](automation-engine-spring/README.md)**         | Spring base layer and SPI for extensibility                | `PluggableAction`, `PluggableCondition`, `PluggableTrigger` |

### Integration Modules

| Module                                                                           | Description                         | Triggers & Actions                                   |
| -------------------------------------------------------------------------------- | ----------------------------------- | ---------------------------------------------------- |
| **[automation-engine-spring-web](automation-engine-spring-web/README.md)**       | HTTP/REST integration               | `onHttpRequest`, `sendHttpRequest`, `onHttpResponse` |
| **[automation-engine-spring-jdbc](automation-engine-spring-jdbc/README.md)**     | Database/JDBC integration           | `onJdbcQuery`, `jdbcQuery`, `jdbcExecute`            |
| **[automation-engine-spring-amqp](automation-engine-spring-amqp/README.md)**     | RabbitMQ/AMQP messaging             | `publishRabbitMqEvent`                               |
| **[automation-engine-spring-events](automation-engine-spring-events/README.md)** | Spring ApplicationEvent integration | `onApplicationEvent`, time-based triggers            |

### Utility & API Modules

| Module                                                                                       | Description                        | Purpose                                    |
| -------------------------------------------------------------------------------------------- | ---------------------------------- | ------------------------------------------ |
| **[automation-engine-test](automation-engine-test/README.md)**                               | Testing utilities and helpers      | `EventCaptureListener`, base test classes  |
| **[automation-engine-backend-api](automation-engine-backend-api/README.md)**                 | REST API for automation management | CRUD operations for automations            |
| **[automation-engine-ui](automation-engine-ui/README.md)**                                   | React-based web UI                 | Visual automation editor and monitor       |
| **[spring-boot-starter-automation-engine](spring-boot-starter-automation-engine/README.md)** | Spring Boot starter (all modules)  | Auto-configuration, dependency aggregation |

### Test Coverage Module

| Module                                     | Description                                             |
| ------------------------------------------ | ------------------------------------------------------- |
| **automation-engine-test-coverage-report** | JaCoCo aggregate coverage report (80% minimum enforced) |

# Key Features

- ✅ **Register and manage automations**: Easily register, remove, and execute automations at runtime
- 🎯 **Event-driven architecture**: Trigger automations in response to custom events or event contexts
- 📝 **Flexible configuration formats**: Define automations programmatically (Java) or via YAML/JSON for dynamic
  configuration
- 🔌 **Pluggable components**: Fully extensible triggers, conditions, actions, variables, and result processors
- 🌱 **Spring Boot integration**: Includes starter modules with auto-configuration for seamless Spring integration
- 🔄 **Interceptor pattern**: Add cross-cutting concerns like logging, metrics, security, and validation
- 🎨 **Template expressions**: Pebble template engine for dynamic value resolution using `{{ expression }}` syntax
- 🧪 **Test utilities**: Built-in testing support with event capture and automation verification
- 🔒 **Thread-safe**: Immutable automation definitions and concurrent execution support
- 📊 **User-defined features**: Create reusable, parameterized actions, conditions, and triggers

# Getting Started

## Installation

### Maven

Add the Spring Boot starter dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>spring-boot-starter-automation-engine</artifactId>
    <version>1.8.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```gradle
implementation 'com.davidrandoll:spring-boot-starter-automation-engine:1.7.0'
```

## Quick Start Example

### 1. Define an Automation (YAML)

Create a file `send-notification.yaml`:

```yaml
alias: send-todo-email-notification
description: Sends an email when a new todo is created
triggers:
  - trigger: onEvent
    eventName: TodoCreateEvent
conditions:
  - condition: template
    expression: "{{ event.priority == 'HIGH' }}"
actions:
  - action: sendEmail
    to: "{{ event.assignee }}"
    subject: "Urgent: New Todo - {{ event.title }}"
    body: |
      Hello {{ event.assignee }},

      A high-priority todo has been created:
      - Title: {{ event.title }}
      - Status: {{ event.status }}
      - Due: {{ event.dueDate }}

      Please review immediately.
```

### 2. Register the Automation

```java
@Service
@RequiredArgsConstructor
public class AutomationService {
    private final AutomationEngine engine;
    private final AutomationCreator factory;

    @PostConstruct
    public void init() {
        String yaml = Files.readString(Path.of("send-notification.yaml"));
        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
    }
}
```

### 3. Trigger the Automation

```java
@RestController
@RequiredArgsConstructor
public class TodoController {
    private final AutomationEngine engine;

    @PostMapping("/todos")
    public ResponseEntity<Todo> createTodo(@RequestBody TodoRequest request) {
        Todo todo = todoService.create(request);

        // Publish event to trigger automation
        TodoCreateEvent event = new TodoCreateEvent(
            todo.getTitle(),
            todo.getAssignee(),
            todo.getStatus(),
            "HIGH",
            todo.getDueDate()
        );
        engine.publishEvent(event);// Automation runs async

        return ResponseEntity.ok(todo);
    }
}
```

## Programmatic Definition

You can also define automations in Java without YAML:

```java
@Component
@RequiredArgsConstructor
public class ProgrammaticAutomation {
    private final AutomationEngine engine;
    private final AutomationProcessor processor;

    @PostConstruct
    public void registerAutomation() {
        // Define variables
        BaseVariableList variables = BaseVariableList.of();

        // Define triggers
        BaseTriggerList triggers = processor.resolveTriggers(List.of(
            TriggerDefinition.builder()
                .trigger("onEvent")
                .param("eventName", "TodoCreateEvent")
                .build()
        ));

        // Define conditions
        BaseConditionList conditions = processor.resolveConditions(List.of(
            ConditionDefinition.builder()
                .condition("template")
                .param("expression", "{{ event.priority == 'HIGH' }}")
                .build()
        ));

        // Define actions
        BaseActionList actions = processor.resolveActions(List.of(
            ActionDefinition.builder()
                .action("logger")
                .param("message", "High priority todo: {{ event.title }}")
                .build()
        ));

        // Create automation
        Automation automation = new Automation(
            "log-high-priority-todos",
            variables,
            triggers,
            conditions,
            actions,
            ec -> null // No result processor
        );

        engine.register(automation);
    }
}
```

## Manual/Imperative Execution

Automations can also be executed imperatively (useful for APIs, scheduled jobs, or CLI tools):

```java
@PostMapping("/api/todos")
public Object createTodoWithAutomation(@RequestBody JsonNode body) {
    IEvent event = engine.getEventFactory().createEvent(body);

    String yaml = """
        alias: create-todo-automation
        triggers:
          - trigger: alwaysTrue
        actions:
          - action: createTodo
            title: "{{ title }}"
            status:
              code: "{{ status }}"
            assignee:
              username: "{{ assignee }}"
            storeToVariable: todo
        result:
          id: "{{ todo.id }}"
          title: "{{ todo.title }}"
          status: "{{ todo.status.code }}"
          assignee: "{{ todo.assignee.username }}"
        """;

    return engine.executeAutomationWithYaml(yaml, event)
            .orElse(null);
}
```

**How It Works:**

- The automation uses `alwaysTrue` trigger so it executes immediately
- Actions create a Todo and store it in the `todo` variable
- The `result` block extracts specific fields and returns them
- Combines declarative YAML with imperative REST endpoint execution

# Extensibility

AutomationEngine is designed with extensibility as a core principle. You can create custom triggers, actions,
conditions, variables, and result processors by implementing the appropriate interfaces and registering them as Spring
beans.

## Extension Pattern: The "Pluggable" SPI

All custom components extend base classes provided by the `automation-engine-spring` module:

- `PluggableAction<T>` - Custom actions
- `PluggableCondition<T>` - Custom conditions
- `PluggableTrigger<T>` - Custom triggers

These base classes provide:

- ✅ Automatic dependency injection for `AutomationProcessor`, `ITypeConverter`, and `ApplicationContext`
- ✅ Access to Spring AOP proxies via `getSelf()` for transaction support, etc.
- ✅ Delegated `AutomationProcessor` methods (e.g., `resolveExpression()`, `executeActions()`)

## Custom Action Example

### 1. Create the Action Class

```java
@Component("sendSlackMessage")  // Bean name = YAML identifier
@Slf4j
public class SendSlackMessageAction extends PluggableAction<SendSlackMessageContext> {

    @Autowired
    private SlackClient slackClient;  // Inject any Spring beans

    @Override
    public void doExecute(EventContext eventContext, SendSlackMessageContext context) {
        String message = context.getMessage();
        String channel = context.getChannel();

        log.info("Sending Slack message to {}: {}", channel, message);
        slackClient.sendMessage(channel, message);
    }
}
```

### 2. Create the Context Class

```java
@Data  // Lombok annotation for getters/setters
public class SendSlackMessageContext implements IActionContext {
    private String alias;        // Required
    private String description;  // Required
    private String message;      // Custom property
    private String channel;      // Custom property
}
```

**Why this pattern?**

- The context class fields are automatically populated from YAML/JSON properties
- `@Data` generates getters/setters needed for property mapping
- `alias` and `description` are mandatory fields from `IActionContext`

### 3. Use in YAML

```yaml
actions:
  - action: sendSlackMessage # Must match @Component name
    alias: notify-team
    description: Notifies the team channel
    message: "New ticket created by {{ event.user }}"
    channel: "#support"
```

## Custom Condition Example

```java
@Component("isBusinessHours")
public class IsBusinessHoursCondition extends PluggableCondition<EmptyConditionContext> {

    @Override
    public boolean isSatisfied(EventContext eventContext, EmptyConditionContext context) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(9, 0)) && !now.isAfter(LocalTime.of(17, 0));
    }
}
```

**Usage in YAML:**

```yaml
conditions:
  - condition: isBusinessHours
```

## Custom Trigger Example

```java
@Component("onSlackMessage")
public class OnSlackMessageTrigger extends PluggableTrigger<OnSlackMessageTriggerContext> {

    @Override
    public boolean isTriggered(EventContext context, OnSlackMessageTriggerContext triggerContext) {
        if (!(context.getEvent() instanceof SlackMessageEvent event)) {
            return false;
        }

        // Check if message matches expected pattern
        return triggerContext.getChannel().equals(event.getChannel());
    }
}
```

**Context Class:**

```java
@Data
public class OnSlackMessageTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private String channel;
}
```

**Usage in YAML:**

```yaml
triggers:
  - trigger: onSlackMessage
    channel: "#general"
```

## Advanced: Using AutomationProcessor

The `PluggableAction`, `PluggableCondition`, and `PluggableTrigger` base classes expose `AutomationProcessor` methods
via `@Delegate`:

```java
@Component("complexAction")
public class ComplexAction extends PluggableAction<ComplexActionContext> {

    @Override
    public void doExecute(EventContext ec, ComplexActionContext ac) {
        // Resolve template expressions
        String resolved = resolveExpression(ec, "{{ event.user.email }}");

        // Execute sub-actions dynamically
        executeActions(ec, List.of(
            ActionDefinition.builder()
                .action("logger")
                .param("message", "Processing for: " + resolved)
                .build()
        ));

        // Check conditions dynamically
        boolean shouldContinue = allConditionsSatisfied(ec, List.of(
            ConditionDefinition.builder()
                .condition("template")
                .param("expression", "{{ event.amount > 1000 }}")
                .build()
        ));

        if (shouldContinue) {
            // Perform action
        }
    }
}
```

## User-Defined Features

AutomationEngine supports **reusable, parameterized components** that can be defined once and used across multiple
automations.

See [USER_DEFINED_FEATURES.md](USER_DEFINED_FEATURES.md) for complete documentation on:

- User-Defined Actions (UDA)
- User-Defined Conditions (UDC)
- User-Defined Triggers (UDT)

**Example:**

Definition:
```yaml
name: sendNotification
conditions: []
variables: []
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
## Testing Custom Components

Use the provided `AutomationEngineTest` base class:

```java
@SpringBootTest
class SendSlackMessageActionTest extends AutomationEngineTest {

    @Test
    void testSlackMessageSent() {
        var yaml = """
            alias: test-slack
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendSlackMessage
                channel: "#test"
                message: "Hello World"
            """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new CustomEvent();
        engine.publishEvent(event);

        // Verify Slack message was sent (use mocks/spies)
        verify(slackClient).sendMessage("#test", "Hello World");
    }
}
```

## Interceptors

Add cross-cutting concerns using interceptors:

```java
@Component
public class LoggingActionInterceptor implements IActionInterceptor {

    @Override
    public void beforeExecution(EventContext ec, IActionContext context) {
        log.info("Executing action: {}", context.getAlias());
    }

    @Override
    public void afterExecution(EventContext ec, IActionContext context) {
        log.info("Completed action: {}", context.getAlias());
    }
}
```

Interceptors are automatically discovered and applied to all actions, conditions, and triggers.

# Inspiration

This project is inspired by [Home Assistant](https://github.com/home-assistant/core), an open-source platform for home
automation that uses YAML to define automations through triggers, conditions, and actions. AutomationEngine brings that
same declarative philosophy to the Java and Spring ecosystem, enabling developers to easily create dynamic, event-driven
workflows across backend services and business logic domains.

## Documentation

- **[AI_PROMPT.md](AI_PROMPT.md)** - Comprehensive YAML/JSON syntax guide with all available triggers, conditions, and
  actions
- **[USER_DEFINED_FEATURES.md](USER_DEFINED_FEATURES.md)** - Guide to creating reusable, parameterized components
- **[Module READMEs](automation-engine-core/README.md)** - Detailed documentation for each module
- **[Examples](examples/)** - Working examples including todo-example with custom actions

## Build & Development

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker (for running integration tests with databases/RabbitMQ)

### Building the Project

```bash
# Build all modules (skip tests)
mvn clean install -DskipTests

# Build with tests and coverage
mvn clean verify

# Build specific module and its dependencies
mvn clean install -pl automation-engine-core -am

# Generate aggregate coverage report
mvn verify -pl automation-engine-test-coverage-report
```

### Testing

AutomationEngine enforces **80% minimum code coverage** (configured in parent pom.xml). The build will fail if coverage
drops below this threshold.

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl automation-engine-spring-web

# Run with coverage
mvn verify

# View aggregate coverage report
open automation-engine-test-coverage-report/target/site/jacoco-aggregate/index.html
```

### Multi-Module Tips

- Always build the parent first or use `-am` (also-make) flag
- Version management uses `${revision}` property (currently 1.7.0)
- Flattened POMs are generated for Maven Central compatibility

## Contributing

Contributions are welcome! If you have suggestions for improvements or bug fixes, feel free to open an issue or submit a
pull request.

### Contribution Workflow

1. **Fork** the repository
2. **Create** a new branch for your feature or bugfix (`feature/my-new-feature`)
3. **Make** your changes and include tests
4. **Ensure** tests pass and coverage meets the 80% minimum: `mvn verify`
5. **Commit** with clear, descriptive messages
6. **Push** to your fork and open a pull request

### Development Guidelines

- Follow existing code style and conventions
- Add unit tests for new functionality (use `AutomationEngineTest` base class)
- Update relevant README files and documentation
- Ensure all tests pass: `mvn clean verify`
- Use Lombok annotations (`@Data`, `@RequiredArgsConstructor`, etc.) for boilerplate reduction
- Context classes must implement appropriate interfaces (`IActionContext`, `IConditionContext`, etc.)
- Always include `alias` and `description` fields in context classes

### Code Style

- Use Java 21 features (text blocks, records, pattern matching)
- Prefer immutability where possible
- Use `@Slf4j` for logging
- Follow builder pattern for complex object construction
- Use `@Component` with descriptive bean names for pluggable components

Thank you for helping improve this project!

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- 📧 **Issues**: [GitHub Issues](https://github.com/david-randoll/AutomationEngine/issues)
- 📖 **Documentation**: See module-specific README files
- 💬 **Discussions**: [GitHub Discussions](https://github.com/david-randoll/AutomationEngine/discussions)

---

**Author:** [david-randoll](https://github.com/david-randoll)
