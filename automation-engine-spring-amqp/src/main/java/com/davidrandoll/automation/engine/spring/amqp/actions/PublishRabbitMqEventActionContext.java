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
    private String alias;
    private String description;
    private Exchange exchange;
    private Queue queue;
    private String routingKey;

    private Map<String, Object> message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    @JsonPropertyOrder({
            Exchange.Fields.name,
            Exchange.Fields.type
    })
    public static class Exchange {
        private String name;
        private ExchangeType type;
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
        private String name;
        private boolean durable;
        private boolean exclusive;
        private boolean autoDelete;
    }
}
