# automation-engine-test

Testing utilities and helpers for AutomationEngine projects.

## Overview

This module provides base classes, utilities, and helpers to simplify testing automations in your projects.

## Features

- ✅ **Base Test Class** - `AutomationEngineTest` with common setup
- ✅ **Event Capture** - `EventCaptureListener` to verify published events
- ✅ **Log Appender** - Capture and assert on log messages
- ✅ **Mock Support** - Integration with Mockito and Spring Boot Test
- ✅ **Automation Verification** - Helper methods to verify automation execution

## Components

### AutomationEngineTest

Base class for automation tests providing common dependencies and setup.

**Provided Fields:**

- `engine` - AutomationEngine instance
- `factory` - AutomationCreator for creating automations
- `logAppender` - TestLogAppender for capturing logs

**Example:**

```java
@SpringBootTest
class MyAutomationTest extends AutomationEngineTest {

    @Test
    void testMyAutomation() {
        var yaml = \"\"\"
            alias: test
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: logger
                message: \"Hello World\"
            \"\"\";

        Automation automation = factory.createAutomation(\"yaml\", yaml);
        engine.register(automation);
        engine.publishEvent(new GenericEvent());

        assertThat(logAppender.getLoggedMessages())
            .anyMatch(msg -> msg.contains(\"Hello World\"));
    }
}
```

### EventCaptureListener

Captures events published during test execution.

**Usage:**

```java
@SpringBootTest
class EventTest extends AutomationEngineTest {

    @Autowired
    private EventCaptureListener eventCapture;

    @Test
    void testEventPublishing() {
        engine.publishEvent(new CustomEvent(\"data\"));

        List<EventContext> captured = eventCapture.getCapturedEvents();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).getEvent()).isInstanceOf(CustomEvent.class);

        eventCapture.clear();
    }
}
```

### TestLogAppender

Captures log messages for verification.

**Methods:**

- `getLoggedMessages()` - Returns List<String> of all logged messages
- `clear()` - Clears captured messages

**Example:**

```java
@Test
void testLogging() {
    var yaml = \"\"\"
        alias: log-test
        triggers:
          - trigger: alwaysTrue
        actions:
          - action: logger
            message: \"Test message\"
        \"\"\";

    Automation automation = factory.createAutomation(\"yaml\", yaml);
    engine.register(automation);
    engine.publishEvent(new GenericEvent());

    assertThat(logAppender.getLoggedMessages())
        .anyMatch(msg -> msg.contains(\"Test message\"));
}
```

## Testing Patterns

### Test Automation Execution

```java
@Test
void testAutomationExecutes() {
    var yaml = \"\"\"
        alias: my-automation
        triggers:
          - trigger: onEvent
            eventName: MyEvent
        actions:
          - action: logger
            message: \"Event received\"
        \"\"\";

    Automation automation = factory.createAutomation(\"yaml\", yaml);
    engine.register(automation);

    MyEvent event = new MyEvent(\"data\");
    engine.publishEvent(event);

    assertThat(logAppender.getLoggedMessages())
        .anyMatch(msg -> msg.contains(\"Event received\"));
}
```

### Test Conditions

```java
@Test
void testConditionPreventsExecution() {
    var yaml = \"\"\"
        alias: conditional
        triggers:
          - trigger: alwaysTrue
        conditions:
          - condition: template
            expression: \"{{ event.value > 10 }}\"
        actions:
          - action: logger
            message: \"Should not execute\"
        \"\"\";

    Automation automation = factory.createAutomation(\"yaml\", yaml);
    engine.register(automation);

    // Event with value <= 10
    EventContext context = EventContext.of(new GenericEvent(Map.of(\"value\", 5)));
    engine.publishEvent(context);

    assertThat(automation.anyTriggerActivated(context)).isTrue();
    assertThat(automation.allConditionsMet(context)).isFalse();
    assertThat(logAppender.getLoggedMessages())
        .noneMatch(msg -> msg.contains(\"Should not execute\"));
}
```

### Test Custom Actions

```java
@SpringBootTest
@Import(MyCustomAction.class)
class CustomActionTest extends AutomationEngineTest {

    @MockBean
    private ExternalService externalService;

    @Test
    void testCustomAction() {
        var yaml = \"\"\"
            alias: custom-test
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: myCustomAction
                param: value
            \"\"\";

        Automation automation = factory.createAutomation(\"yaml\", yaml);
        engine.register(automation);
        engine.publishEvent(new GenericEvent());

        verify(externalService).doSomething(\"value\");
    }
}
```

### Test HTTP Triggers

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpTriggerTest extends AutomationEngineTest {

    @Test
    void testHttpRequestTrigger() {
        var yaml = \"\"\"
            alias: http-test
            triggers:
              - trigger: onHttpRequest
                method: POST
                path: \"/api/test\"
            actions:
              - action: logger
                message: \"HTTP request received\"
            \"\"\";

        Automation automation = factory.createAutomation(\"yaml\", yaml);
        engine.register(automation);

        AEHttpRequestEvent event = AEHttpRequestEvent.builder()
            .method(HttpMethodEnum.POST)
            .path(\"/api/test\")
            .build();

        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
            .anyMatch(msg -> msg.contains(\"HTTP request received\"));
    }
}
```

### Test JDBC Actions

```java
@SpringBootTest
@Import(TestConfig.class)
class JdbcActionTest extends AutomationEngineTest {

    @MockBean
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void testJdbcQuery() {
        when(jdbcTemplate.queryForList(any(), any()))
            .thenReturn(List.of(Map.of(\"count\", 5)));

        var yaml = \"\"\"
            alias: jdbc-test
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: jdbcQuery
                query: \"SELECT COUNT(*) as count FROM tasks\"
                variable: result
                mode: single
            \"\"\";

        Automation automation = factory.createAutomation(\"yaml\", yaml);
        engine.register(automation);
        engine.publishEvent(new GenericEvent());

        verify(jdbcTemplate).queryForList(any(), any());
    }
}
```

## Best Practices

1. **Use AutomationEngineTest base class** - Provides common setup
2. **Clear captured data** - Call `logAppender.clear()` and `eventCapture.clear()` in `@BeforeEach`
3. **Mock external services** - Use `@MockBean` for external dependencies
4. **Test edge cases** - Test with empty data, null values, etc.
5. **Verify execution flow** - Test triggers, conditions, and actions separately
6. **Use AssertJ** - Fluent assertions for better readability

## Dependencies

- `automation-engine-core` - Core automation logic
- `org.springframework.boot:spring-boot-starter-test` - Spring Boot testing
- `org.assertj:assertj-core` - Fluent assertions

## See Also

- **[Examples](../examples/todo-example/)** - Working test examples
- **[automation-engine-core](../automation-engine-core/README.md)** - Core concepts
