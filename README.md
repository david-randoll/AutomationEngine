# AutomationEngine

AutomationEngine is a flexible and extensible Java library for building event-driven automation workflows. It allows
developers to define and execute automations using YAML, JSON, or Java code. Automations respond to incoming events and
context, making it easy to implement dynamic business logic in a structured, declarative way.

# Overview

AutomationEngine is built for:

- Defining automation logic through code or configuration
- Responding to events in real time
- Dynamically executing actions based on conditions
- Seamless integration into Spring Boot applications

It provides a modular architecture where triggers, conditions, and actions are all pluggable and customizable.

# Key Features

- **Register and manage automations**: Easily register, remove, and execute automations at runtime.
- **Event-driven**: Trigger automations in response to custom events or event contexts.
- **Flexible formats**: Define automations programmatically or via YAML/JSON for dynamic configuration.
- **Spring integration**: Includes starter modules for seamless integration with Spring applications.
- **Extensible**: Fully extensible for custom triggers, variables, conditions, result and actions

# Getting Started

## Installation

To use AutomationEngine with Spring Boot, add the following dependency:

```xml

<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>spring-boot-starter-automation-engine</artifactId>
    <version>${revision}</version>
</dependency>
```

## Defining Automations

Automations can be created from configuration files or defined inline. Each automation has a set of triggers, optional
conditions, and one or more actions.

### YAML Example

This automation sends an email whenever a `TodoCreateEvent` occurs:

```yaml
alias: send-todo-email-notification
triggers:
  - trigger: onEvent
    eventName: TodoCreateEvent
actions:
  - action: sendEmail
    to: "{{ event.assignee }}"
    subject: "New Todo Created: {{ event.title }}"
    body: |
      Hello {{ event.assignee }},

      A new todo has been created:
      - Title: {{ event.title }}
      - Status: {{ event.status }}

      Please review this task.
```

### Programmatic Usage

```java
AutomationEngine engine; // inject from spring context
AutomationCreator factory;// inject from spring context

public void run() {
    String yaml = "...";
    Automation automation = factory.createAutomation("yaml", yaml);
    engine.register(automation);
}
```

To publish an event and trigger matching automations:

```java
public void run() {
    TodoCreateEvent event = new TodoCreateEvent("Buy groceries", "johndoe@example.com", "OPEN");
    engine.publishEvent(event); // This triggers the automation
}
```

## Manual Execution

Automations can also be executed imperatively, which is useful in APIs, scheduled jobs, or CLI tools.

```java

@PostMapping
public Object createTodoWithStatusAndAssigneeObject(@RequestBody JsonNode body) {
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
              id: "{{todo.id}}"
              title: "{{ todo.title }}"
              status: "{{ todo.status.code }}"
              assignee: "{{ todo.assignee.username }}"
            """;
    return engine.executeAutomationWithYaml(yaml, event)
            .orElse(null);
}
```

#### How It Works

- The automation is defined as a YAML string inside the method.
- The trigger is set to `"alwaysTrue"` so it always executes when called.
- The action creates a Todo using data from the event/request.
- The result block extracts and returns the details of the created Todo.
- The automation is executed manually via `engine.executeAutomationWithYaml(yaml, event)`.

This pattern allows you to leverage all the power of AutomationEngine even in imperative workflows, such as REST
endpoints or CLI tools.

# Extensibility

AutomationEngine is designed with extensibility in mind. You can define your own custom triggers, actions, conditions,
result processors, and variable resolvers by implementing the appropriate interfaces and registering them with the
engine.

## Custom Action Example

To create your own action:

```java

@Component("sendSlackMessage")
public class SendSlackMessageAction extends PluggableAction<SendSlackMessageContext> {

    @Override
    public void doExecute(EventContext eventContext, SendSlackMessageContext context) {
        String message = context.getMessage();
        String channel = context.getChannel();
        // Send message to Slack...
    }
}
```

Create a corresponding context class:

```java

@Data
public class SendSlackMessageContext extends ActionContext {
    private String message;
    private String channel;
}
```

Now you can use it in YAML:

```yaml
- alias: send-slack-notification
  action: sendSlackMessage
  message: "New ticket created by {{ event.user }}"
  channel: "#support"
```

## Custom Condition Example

```java

@Component("isBusinessHours")
public class IsBusinessHoursCondition extends PluggableCondition<EmptyConditionContext> {

    @Override
    public boolean evaluate(EventContext eventContext, EmptyConditionContext context) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(9, 0)) && !now.isAfter(LocalTime.of(17, 0));
    }
}
```

In YAML:

```yaml
- alias: check-business-hours
  condition: isBusinessHours
```

## Custom Trigger Example

```java

@Component("onSlackMessage")
public class OnSlackMessageTrigger extends PluggableTrigger<OnSlackMessageTriggerContext> {

    @Override
    public boolean isTriggered(EventContext context, OnSlackMessageTriggerContext triggerContext) {
        return "SlackMessageEvent".equals(context.getEventName());
    }
}
```

In YAML:

```yaml
- alias: on-slack-message
  trigger: onSlackMessage
```

# Inspiration

This project is inspired by [Home Assistant](https://github.com/home-assistant/core), an open-source platform for home
automation that uses YAML to define automations through triggers, conditions, and actions. AutomationEngine brings that
same declarative philosophy to the Java and Spring ecosystem, enabling developers to easily create dynamic, event-driven
workflows across backend services and business logic domains.

## Contributing

Contributions are welcome! If you have suggestions for improvements or bug fixes, feel free to open an issue or submit a
pull request.

To contribute:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Make your changes and include tests if applicable.
4. Open a pull request describing your changes.

Please ensure your code follows the existing style and passes any automated checks.

Thank you for helping improve this project!

---

**Author:** [david-randoll](https://github.com/david-randoll)