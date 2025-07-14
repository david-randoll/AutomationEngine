# AutomationEngine

AutomationEngine is a flexible, extensible Java library for building, managing, and executing automations based on
events and context. It provides core abstractions and utilities to define automations using code, YAML, or JSON formats,
and enables event-driven automation workflows.

# Features

- **Register and manage automations**: Easily register, remove, and execute automations at runtime.
- **Event-driven**: Trigger automations in response to custom events or event contexts.
- **Flexible formats**: Define automations programmatically or via YAML/JSON for dynamic configuration.
- **Spring integration**: Includes starter modules for seamless integration with Spring applications.
- **Extensible**: Core utilities for reflection and type resolution, plus pluggable action, trigger, condition, and
  result builders.

# Getting Started

## Installation

Add the AutomationEngine core library to your project. If you're using Maven:

```xml

<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-spring-starter</artifactId>
    <version>0.0.1-beta</version>
</dependency>
```

## Usage

To use AutomationEngine, you can define automations in YAML or JSON format, or programmatically using Java code.

### Example: Todo Domain Automation

Suppose you want to automate sending an email notification whenever a new todo is created. The automation will trigger
on a custom event (`TodoCreateEvent`), extract details from the event, and then perform an email action using those
details.

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

### Registering the Automation

```java
AutomationEngine engine; // inject from spring context
AutomationCreator factory;// inject from spring context

String yaml = "...";
Automation automation = factory.createAutomation("yaml", yaml);
engine.register(automation);
```

### Publishing the Event

```java
TodoCreateEvent event = new TodoCreateEvent("Buy groceries", "johndoe@example.com", "OPEN");
engine.publishEvent(event); // This triggers the automation
```

## Manually Running Automations

In addition to event-driven triggers, AutomationEngine supports manually running automations. This is particularly useful for scenarios such as API endpoints, scheduled jobs, or user-initiated actions. You can execute an automation directly by providing its definition and the event/context.

### Example: Manually Executing an Automation

Suppose you want to create a Todo item with specific status and assignee, and return the result. You can define the automation YAML inline and execute it manually using the API:

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

This pattern allows you to leverage all the power of AutomationEngine even in imperative workflows, such as REST endpoints or CLI tools.

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

## License

MIT License

---

**Author:** [david-randoll](https://github.com/david-randoll)