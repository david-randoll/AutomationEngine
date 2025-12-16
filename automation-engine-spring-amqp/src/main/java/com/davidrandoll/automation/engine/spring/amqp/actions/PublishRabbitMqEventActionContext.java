package com.davidrandoll.automation.engine.spring.amqp.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        PublishRabbitMqEventActionContext.Fields.alias,
        PublishRabbitMqEventActionContext.Fields.description,
        PublishRabbitMqEventActionContext.Fields.exchange,
        PublishRabbitMqEventActionContext.Fields.queue,
        PublishRabbitMqEventActionContext.Fields.routingKey,
        PublishRabbitMqEventActionContext.Fields.message
})
public class PublishRabbitMqEventActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** RabbitMQ exchange configuration for publishing the message */
    private Exchange exchange;

    /** RabbitMQ queue configuration for publishing the message */
    private Queue queue;

    /** Routing key for message routing. Defaults to empty string */
    private String routingKey = "";

    /** Message payload to publish to RabbitMQ. Can contain any key-value pairs */
    private Map<String, Object> message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    @JsonPropertyOrder({
            Exchange.Fields.name,
            Exchange.Fields.type,
            Exchange.Fields.durable,
            Exchange.Fields.autoDelete
    })
    public static class Exchange {
        /** Name of the RabbitMQ exchange */
        private String name;

        /** Type of exchange (DIRECT, TOPIC, or FANOUT) */
        private ExchangeType type;

        /** Whether the exchange should survive broker restart. Defaults to true */
        private boolean durable = true;

        /** Whether the exchange should be deleted when no longer in use. Defaults to false */
        private boolean autoDelete = false;
    }

    public enum ExchangeType {
        DIRECT,
        TOPIC,
        FANOUT
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    @JsonPropertyOrder({
            Queue.Fields.name,
            Queue.Fields.durable,
            Queue.Fields.exclusive,
            Queue.Fields.autoDelete
    })
    public static class Queue {
        /** Name of the RabbitMQ queue */
        private String name;

        /** Whether the queue should survive broker restart. Defaults to true */
        private boolean durable = true;

        /** Whether the queue should be used by only one connection. Defaults to false */
        private boolean exclusive = false;

        /** Whether the queue should be deleted when no longer in use. Defaults to false */
        private boolean autoDelete = false;
    }
}
