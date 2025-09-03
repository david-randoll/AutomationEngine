package com.davidrandoll.automation.engine.spring.amqp.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    public static class Queue {
        private String name;
        private boolean durable;
        private boolean exclusive;
        private boolean autoDelete;
    }
}
