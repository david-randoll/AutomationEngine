# automation-engine-spring-amqp

RabbitMQ/AMQP messaging integration module for publishing events to message brokers.

## Overview

This module enables automations to publish messages to RabbitMQ exchanges and queues, integrating AutomationEngine with
messaging systems.

## Features

- ✅ **Publish to Exchange** - Send messages to RabbitMQ exchanges
- ✅ **Publish to Queue** - Send messages directly to queues
- ✅ **Routing Keys** - Support for routing key patterns
- ✅ **Template Support** - Use `{{ }}` expressions in messages and routing keys
- ✅ **Auto-Declare** - Automatically declare queues if they don't exist

## Components

### publishRabbitMqEvent Action

Publishes a message to a RabbitMQ exchange or queue.

**Parameters:**

- `exchange` (string, optional) - Exchange name
- `queue` (string, optional) - Queue name (for direct publishing)
- `routingKey` (string, optional) - Routing key
- `message` (object) - Message payload (can be string, map, or any object)

**Example - Publish to Exchange:**

```yaml
actions:
  - action: publishRabbitMqEvent
    exchange: orders-exchange
    routingKey: order.created
    message:
      orderId: "{{ event.orderId }}"
      customerId: "{{ event.customerId }}"
      amount: "{{ event.amount }}"
```

**Example - Publish to Queue:**

```yaml
actions:
  - action: publishRabbitMqEvent
    queue: notifications-queue
    message:
      type: email
      to: "{{ event.user.email }}"
      subject: "Order Confirmation"
      body: "Your order has been confirmed"
```

**Example - With Routing Key Pattern:**

```yaml
actions:
  - action: publishRabbitMqEvent
    exchange: events
    routingKey: "user.{{ event.action }}.{{ event.userId }}"
    message:
      action: "{{ event.action }}"
      timestamp: "{{ now }}"
      userId: "{{ event.userId }}"
```

## Real-World Examples

### Order Processing

```yaml
alias: process-new-order
triggers:
  - trigger: onEvent
    eventName: OrderCreatedEvent
actions:
  - action: publishRabbitMqEvent
    exchange: orders
    routingKey: order.created
    message:
      orderId: "{{ event.orderId }}"
      status: PENDING
      timestamp: "{{ now }}"

  - action: publishRabbitMqEvent
    queue: order-notifications
    message:
      type: confirmation
      email: "{{ event.customerEmail }}"
      orderId: "{{ event.orderId }}"
```

### Event Broadcasting

```yaml
alias: broadcast-user-events
triggers:
  - trigger: onEvent
    eventName: UserEvent
actions:
  - action: publishRabbitMqEvent
    exchange: user-events
    routingKey: "user.{{ event.eventType }}"
    message:
      userId: "{{ event.userId }}"
      eventType: "{{ event.eventType }}"
      data: "{{ event.data }}"
      timestamp: "{{ now }}"
```

### Task Distribution

```yaml
alias: distribute-background-jobs
triggers:
  - trigger: onHttpRequest
    method: POST
    path: "/api/jobs"
actions:
  - action: publishRabbitMqEvent
    queue: background-jobs
    message:
      jobType: "{{ event.requestBody.type }}"
      priority: "{{ event.requestBody.priority }}"
      params: "{{ event.requestBody.params }}"
```

## Configuration

### RabbitMQ Setup

```properties
# application.properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=username
spring.rabbitmq.password=password
```

### Exchange and Queue Declaration

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange("orders-exchange");
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue("notifications-queue", true);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with("order.#");
    }
}
```

## Testing

```java
@SpringBootTest
class RabbitMqActionTest extends AutomationEngineTest {

    @MockBean
    private AmqpTemplate amqpTemplate;

    @Test
    void testPublishToExchange() {
        var yaml = """
            alias: test-publish
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: publishRabbitMqEvent
                exchange: test-exchange
                routingKey: test.key
                message:
                  data: value
            """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        engine.publishEvent(new CustomEvent());

        verify(amqpTemplate).convertAndSend(
            eq("test-exchange"),
            eq("test.key"),
            any()
        );
    }
}
```

## Dependencies

- `automation-engine-spring` - Spring integration
- `org.springframework.boot:spring-boot-starter-amqp` - RabbitMQ support

## See Also

- **[automation-engine-spring](../automation-engine-spring/README.md)** - Spring integration
- **[RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)** - RabbitMQ official docs
