# automation-engine-spring-events

Spring ApplicationEvent integration module for time-based and event-based automations.

## Overview

This module provides Spring Framework event integration and time-based scheduling capabilities for AutomationEngine.

## Features

- ✅ **Time-Based Triggers** - Cron and fixed-time scheduling
- ✅ **Application Events** - Integration with Spring's ApplicationEvent system
- ✅ **Event Publishing** - Publish Spring events from automations
- ✅ **Scheduled Execution** - Run automations on schedules

## Components

### 1. Time-Based Triggers

**time Trigger** - Schedule automations using cron expressions or fixed times.

**Parameters:**

- `at` (string) - Fixed time in `HH:mm` format
- `cron` (string) - Cron expression

**Example - Fixed Time:**

```yaml
triggers:
  - trigger: time
    at: \"22:37\"
actions:
  - action: logger
    message: \"Daily task executed at 10:37 PM\"
```

**Example - Cron Expression:**

```yaml
triggers:
  - trigger: time
    cron: \"0 0 * * *\" # Daily at midnight
actions:
  - action: logger
    message: \"Daily cleanup started\"
```

**Cron Patterns:**

```yaml
# Every 5 minutes
cron: \"*/5 * * * *\"

# Every hour at minute 0
cron: \"0 * * * *\"

# Every day at 8 AM
cron: \"0 8 * * *\"

# Every Monday at 9 AM
cron: \"0 9 * * 1\"

# First day of month at midnight
cron: \"0 0 1 * *\"
```

### 2. Application Event Trigger

**onApplicationEvent** - Trigger on Spring ApplicationEvents.

**Example:**

```yaml
triggers:
  - trigger: onApplicationEvent
    eventType: com.example.UserCreatedEvent
actions:
  - action: logger
    message: \"User created: {{ event.username }}\"
```

## Real-World Examples

### Daily Reports

```yaml
alias: daily-report
description: Generate and send daily reports
triggers:
  - trigger: time
    at: \"08:00\"
actions:
  - action: jdbcQuery
    query: \"SELECT COUNT(*) as count FROM orders WHERE DATE(created_at) = CURRENT_DATE\"
    variable: dailyOrders
    mode: single

  - action: sendEmail
    to: \"management@example.com\"
    subject: \"Daily Orders Report\"
    body: \"Orders today: {{ dailyOrders.count }}\"
```

### Scheduled Cleanup

```yaml
alias: cleanup-expired-sessions
triggers:
  - trigger: time
    cron: \"0 2 * * *\" # 2 AM daily
actions:
  - action: jdbcExecute
    query: \"DELETE FROM sessions WHERE last_activity < NOW() - INTERVAL 7 DAY\"

  - action: logger
    message: \"Session cleanup completed\"
```

### Hourly Health Checks

```yaml
alias: service-health-check
triggers:
  - trigger: time
    cron: \"0 * * * *\" # Every hour
actions:
  - action: sendHttpRequest
    url: \"http://localhost:8080/actuator/health\"
    method: GET
    storeToVariable: health

  - action: sendEmail
    condition: \"{{ health.status != 'UP' }}\"
    to: \"ops@example.com\"
    subject: \"Service Health Alert\"
    body: \"Service is {{ health.status }}\"
```

## Events

### TimeBasedEvent

Published when time-based triggers activate.

```java
public class TimeBasedEvent implements IEvent {
    private final LocalTime time;
}
```

**Accessing in templates:**

```yaml
actions:
  - action: logger
    message: \"Triggered at {{ event.time }}\"
```

## Configuration

### Enable Scheduling

Ensure `@EnableScheduling` is present:

```java
@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Custom Scheduler

```java
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }
}
```

## Testing

```java
@SpringBootTest
class TimeBasedTriggerTest extends AutomationEngineTest {

    @Test
    void testTimeTrigger() {
        var yaml = \"\"\"
            alias: test-time
            triggers:
              - trigger: time
                at: \"{{ now }}\"
            actions:
              - action: logger
                message: \"Time trigger activated\"
            \"\"\";

        Automation automation = factory.createAutomation(\"yaml\", yaml);
        engine.register(automation);

        // Publish time event
        TimeBasedEvent event = new TimeBasedEvent(LocalTime.now());
        engine.publishEvent(event);

        assertThat(logAppender.getLoggedMessages())
            .anyMatch(msg -> msg.contains(\"Time trigger activated\"));
    }
}
```

## Dependencies

- `automation-engine-spring` - Spring integration
- `org.springframework:spring-context` - Spring context and events

## See Also

- **[automation-engine-spring](../automation-engine-spring/README.md)** - Spring integration
- **[Spring Scheduling](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)** -
  Spring scheduling docs
