package com.davidrandoll.automation.engine.spring.amqp.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
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
    @ContextField(
        helpText = "Configure the RabbitMQ exchange for message routing"
    )
    private Exchange exchange;

    /** RabbitMQ queue configuration for publishing the message */
    @ContextField(
        helpText = "Configure the RabbitMQ queue directly (alternative to exchange)"
    )
    private Queue queue;

    /** Routing key for message routing. Defaults to empty string */
    @ContextField(
        placeholder = "orders.created",
        helpText = "Routing key for message routing. Used with TOPIC and DIRECT exchanges"
    )
    private String routingKey = "";

    /** Message payload to publish to RabbitMQ. Can contain any key-value pairs */
    @ContextField(
        helpText = "Message payload as key-value pairs. Values can use {{ }} templates"
    )
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
        @ContextField(
            placeholder = "my-exchange",
            helpText = "Name of the RabbitMQ exchange"
        )
        private String name;

        /** Type of exchange (DIRECT, TOPIC, or FANOUT) */
        @ContextField(
            widget = ContextField.Widget.DROPDOWN,
            dropdownOptions = {"DIRECT", "TOPIC", "FANOUT"},
            dropdownLabels = {"Direct - exact routing key match", "Topic - pattern routing key", "Fanout - broadcast to all queues"},
            helpText = "Exchange type determines how messages are routed to queues"
        )
        private ExchangeType type;

        /** Whether the exchange should survive broker restart. Defaults to true */
        @ContextField(
            widget = ContextField.Widget.SWITCH,
            helpText = "Durable exchanges survive broker restart"
        )
        private boolean durable = true;

        /** Whether the exchange should be deleted when no longer in use. Defaults to false */
        @ContextField(
            widget = ContextField.Widget.SWITCH,
            helpText = "Auto-delete exchanges are removed when no queues are bound"
        )
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
        @ContextField(
            placeholder = "my-queue",
            helpText = "Name of the RabbitMQ queue"
        )
        private String name;

        /** Whether the queue should survive broker restart. Defaults to true */
        @ContextField(
            widget = ContextField.Widget.SWITCH,
            helpText = "Durable queues survive broker restart"
        )
        private boolean durable = true;

        /** Whether the queue should be used by only one connection. Defaults to false */
        @ContextField(
            widget = ContextField.Widget.SWITCH,
            helpText = "Exclusive queues can only be used by one connection"
        )
        private boolean exclusive = false;

        /** Whether the queue should be deleted when no longer in use. Defaults to false */
        @ContextField(
            widget = ContextField.Widget.SWITCH,
            helpText = "Auto-delete queues are removed when no consumers are connected"
        )
        private boolean autoDelete = false;
    }
}
