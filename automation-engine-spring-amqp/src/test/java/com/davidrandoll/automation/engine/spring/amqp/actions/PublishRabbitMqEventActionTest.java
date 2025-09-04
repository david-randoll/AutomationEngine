package com.davidrandoll.automation.engine.spring.amqp.actions;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(TestConfig.class)
class PublishRabbitMqEventActionTest extends AutomationEngineTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @BeforeEach
    void setup() {
        reset(amqpTemplate, amqpAdmin);
    }

    @Test
    void testPublishMessageWithDirectExchangeAndQueue() {
        var yaml = """
                alias: Publish test message
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: publishRabbitMqEvent
                    exchange:
                      name: test-exchange
                      type: DIRECT
                      durable: true
                      autoDelete: false
                    queue:
                      name: test-queue
                      durable: true
                      exclusive: false
                      autoDelete: false
                    routingKey: test.key
                    message:
                      text: "Hello World"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Verify exchange and queue declared
        verify(amqpAdmin).declareExchange(any());
        verify(amqpAdmin).declareQueue(any());
        verify(amqpAdmin).declareBinding(any());

        // Verify message sent
        verify(amqpTemplate).convertAndSend(eq("test-exchange"), eq("test.key"), eq(Map.of("text", "Hello World")));
    }

    @Test
    void testPublishMessageWithoutQueueFanoutExchange() {
        var yaml = """
                alias: Publish fanout message
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: publishRabbitMqEvent
                    exchange:
                      name: fanout-exchange
                      type: FANOUT
                    routingKey: ""
                    message:
                      text: "Broadcast message"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(11, 0)));
        engine.publishEvent(context);

        // Fanout exchange: no queue binding, just declare exchange
        verify(amqpAdmin).declareExchange(any());
        verify(amqpAdmin, never()).declareQueue(any());
        verify(amqpAdmin, never()).declareBinding(any());

        verify(amqpTemplate).convertAndSend(eq("fanout-exchange"), eq(""), eq(Map.of("text", "Broadcast message")));
    }

    @Test
    void testMissingExchangeThrowsException() {
        var yaml = """
                alias: Missing exchange
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: publishRabbitMqEvent
                    message:
                      text: "Hello"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(12, 0)));

        // Expect exception due to missing exchange
        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Exchange configuration must be provided");

        // Ensure no message sent
        verifyNoInteractions(amqpTemplate);
        verifyNoInteractions(amqpAdmin);
    }
}