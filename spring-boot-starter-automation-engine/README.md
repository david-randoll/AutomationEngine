# spring-boot-starter-automation-engine

Spring Boot starter that aggregates all AutomationEngine modules for easy integration.

## Overview

This starter provides a single dependency that includes all AutomationEngine modules, making it the simplest way to add
automation capabilities to your Spring Boot application.

## Features

- ✅ **Single Dependency** - All modules included
- ✅ **Auto-Configuration** - Zero configuration required
- ✅ **Full Feature Set** - HTTP, JDBC, AMQP, Events, UI, and API
- ✅ **Spring Boot 3.x** - Modern Spring Boot support
- ✅ **Production Ready** - Tested and optimized

## Quick Start

### 1. Add Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>spring-boot-starter-automation-engine</artifactId>
    <version>1.7.0</version>
</dependency>
```

Or in Gradle:

```gradle
implementation 'com.davidrandoll:spring-boot-starter-automation-engine:1.7.0'
```

### 2. Create Automation

Create `automations/welcome.yaml` in `src/main/resources`:

```yaml
alias: welcome-automation
description: Log a welcome message on startup
triggers:
  - trigger: alwaysTrue
actions:
  - action: logger
    message: "AutomationEngine is ready!"
```

### 3. Load Automation

```java
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationCreator factory;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String yaml = new String(Files.readAllBytes(
            Paths.get("src/main/resources/automations/welcome.yaml")
        ));

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        engine.publishEvent(new GenericEvent());
    }
}
```

### 4. Run Application

```bash
mvn spring-boot:run
```

You should see: `AutomationEngine is ready!` in the logs.

## Included Modules

This starter includes:

| Module                              | Purpose                            |
| ----------------------------------- | ---------------------------------- |
| **automation-engine-core**          | Core automation logic (no Spring)  |
| **automation-engine-templating**    | Pebble template engine integration |
| **automation-engine-spring**        | Spring base layer & SPI            |
| **automation-engine-spring-web**    | HTTP triggers/actions              |
| **automation-engine-spring-jdbc**   | Database triggers/actions          |
| **automation-engine-spring-amqp**   | RabbitMQ support                   |
| **automation-engine-spring-events** | Spring event integration           |
| **automation-engine-test**          | Testing utilities                  |
| **automation-engine-backend-api**   | REST API for automation management |
| **automation-engine-ui**            | React UI for building automations  |

## Auto-Configuration

The starter automatically configures:

1. **AutomationEngine** - Main API facade
2. **AutomationCreator** - Factory for creating automations
3. **Component Scanning** - All pluggable actions, conditions, and triggers
4. **Template Engine** - Pebble template processor
5. **REST API** - Backend API at `/automation-engine/**`
6. **UI** - React UI at `/automation-engine-ui` (if enabled)

## Configuration Properties

Configure AutomationEngine in `application.yml`:

```yaml
automation-engine:
  # UI Configuration
  ui:
    enabled: true
    path: /automation-engine-ui
```

## Usage Examples

### HTTP Webhook

```yaml
alias: webhook-handler
description: Handle incoming webhooks
triggers:
  - trigger: onHttpRequest
    method: POST
    path: /webhooks/github
actions:
  - action: logger
    message: "Webhook received from {{ event.headers.X-GitHub-Event }}"

  - action: jdbcExecute
    query: "INSERT INTO webhook_log (event, data) VALUES (:event, :data)"
    params:
      event: "{{ event.headers.X-GitHub-Event }}"
      data: "{{ event.body | json }}"
```

### Scheduled Database Cleanup

```yaml
alias: cleanup-old-data
description: Delete old records daily
triggers:
  - trigger: time
    cron: "0 2 * * *" # 2 AM daily
actions:
  - action: jdbcExecute
    query: "DELETE FROM audit_logs WHERE created_at < NOW() - INTERVAL 90 DAY"

  - action: logger
    message: "Cleanup completed"
```

### RabbitMQ Event Publishing

```yaml
alias: order-created-event
description: Publish event when order is created
triggers:
  - trigger: onJdbcQuery
    query: "SELECT * FROM orders WHERE status = 'NEW' AND published = false"
    pollInterval: 5000
actions:
  - action: publishRabbitMqEvent
    exchange: orders
    routingKey: order.created
    message: "{{ row | json }}"

  - action: jdbcExecute
    query: "UPDATE orders SET published = true WHERE id = :id"
    params:
      id: "{{ row.id }}"
```

### Conditional Email Alerts

```yaml
alias: high-value-order-alert
description: Alert on high-value orders
triggers:
  - trigger: onJdbcQuery
    query: "SELECT * FROM orders WHERE total > 10000 AND alerted = false"
    pollInterval: 10000
conditions:
  - condition: template
    expression: "{{ row.total > 10000 }}"
actions:
  - action: sendEmail
    to: "sales@example.com"
    subject: "High Value Order Alert"
    body: "Order #{{ row.id }} - Total: ${{ row.total }}"

  - action: jdbcExecute
    query: "UPDATE orders SET alerted = true WHERE id = :id"
    params:
      id: "{{ row.id }}"
```

## Creating Custom Components

### Custom Action

```java
@Component("myAction")
@Slf4j
public class MyCustomAction extends PluggableAction<MyActionContext> {

    @Override
    public void doExecute(EventContext ec, MyActionContext ac) {
        log.info("Executing custom action: {}", ac.getMessage());
    }
}

@Data
public class MyActionContext implements IActionContext {
    private String alias;
    private String description;
    private String message;
}
```

**Usage:**

```yaml
actions:
  - action: myAction
    message: "Hello from custom action!"
```

### Custom Condition

```java
@Component("isBusinessHours")
public class IsBusinessHoursCondition extends PluggableCondition<EmptyConditionContext> {

    @Override
    public boolean evaluate(EventContext ec, EmptyConditionContext ctx) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(9, 0)) &&
               !now.isAfter(LocalTime.of(17, 0));
    }
}
```

**Usage:**

```yaml
conditions:
  - condition: isBusinessHours
```

### Custom Trigger

```java
@Component("onFileChange")
public class OnFileChangeTrigger extends PluggableTrigger<OnFileChangeTriggerContext> {

    @Override
    public boolean isActivated(EventContext ec, OnFileChangeTriggerContext tc) {
        if (!(ec.getEvent() instanceof FileChangeEvent)) {
            return false;
        }
        FileChangeEvent event = (FileChangeEvent) ec.getEvent();
        return event.getPath().equals(tc.getPath());
    }
}

@Data
public class OnFileChangeTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private String path;
}
```

**Usage:**

```yaml
triggers:
  - trigger: onFileChange
    path: "/var/log/app.log"
```

## Testing

### Test Dependencies

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Test Example

```java
@SpringBootTest
class MyAutomationTest extends AutomationEngineTest {

    @Test
    void testAutomation() {
        var yaml = """
            alias: test
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: logger
                message: "Test message"
            """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        engine.publishEvent(new GenericEvent());

        assertThat(logAppender.getLoggedMessages())
            .anyMatch(msg -> msg.contains("Test message"));
    }
}
```

## Accessing the UI

Once your application is running, access the UI at:

```
http://localhost:8080/automation-engine-ui
```

The UI provides:

- Visual automation builder
- Component browser
- Schema documentation
- YAML/JSON preview

## Production Considerations

### Disable UI in Production

```yaml
automation-engine:
  ui:
    enabled: false
```

### Secure API Endpoints

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/automation-engine/**").authenticated()
            .anyRequest().permitAll()
        );
        return http.build();
    }
}
```

### Monitor Automation Execution

```java
@Component
public class AutomationMetricsInterceptor implements IAutomationExecutionInterceptor {

    private final MeterRegistry meterRegistry;

    @Override
    public void beforeExecution(Automation automation, EventContext ec) {
        meterRegistry.counter("automation.executed",
            "alias", automation.getAlias()).increment();
    }
}
```

## Requirements

- Java 21+
- Spring Boot 3.5.x
- Maven 3.8+ or Gradle 8+

## Dependencies

This starter has the following transitive dependencies:

- Spring Boot Starter Web
- Spring Boot Starter JDBC
- Spring Boot Starter AMQP (optional)
- Pebble Template Engine
- Jackson (JSON/YAML processing)
- Lombok (optional, for cleaner code)

## See Also

- **[Main README](../README.md)** - Project overview
- **[AI_PROMPT.md](../AI_PROMPT.md)** - Complete YAML/JSON syntax guide
- **[USER_DEFINED_FEATURES.md](../USER_DEFINED_FEATURES.md)** - User-defined components
- **[Examples](../examples/)** - Working examples
