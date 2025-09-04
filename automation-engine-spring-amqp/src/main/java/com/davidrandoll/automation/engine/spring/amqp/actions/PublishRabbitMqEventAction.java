package com.davidrandoll.automation.engine.spring.amqp.actions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class PublishRabbitMqEventAction extends PluggableAction<PublishRabbitMqEventActionContext> {
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    @Override
    public void doExecute(EventContext eventContext, PublishRabbitMqEventActionContext ctx) {
        if (ctx.getExchange() == null || ObjectUtils.isEmpty(ctx.getExchange().getName())) {
            throw new IllegalArgumentException("Exchange configuration must be provided");
        }

        String exchangeName = ctx.getExchange().getName();
        boolean exchangeDurable = ctx.getExchange().isDurable();
        boolean exchangeAutoDelete = ctx.getExchange().isAutoDelete();
        String routingKey = ctx.getRoutingKey();

        Exchange exchange = switch (ctx.getExchange().getType()) {
            case FANOUT -> new FanoutExchange(exchangeName, exchangeDurable, exchangeAutoDelete);
            case TOPIC -> new TopicExchange(exchangeName, exchangeDurable, exchangeAutoDelete);
            case DIRECT -> new DirectExchange(exchangeName, exchangeDurable, exchangeAutoDelete);
        };
        amqpAdmin.declareExchange(exchange);

        if (ctx.getQueue() != null && !ObjectUtils.isEmpty(ctx.getQueue().getName())) {
            Queue queue = new Queue(
                    ctx.getQueue().getName(),
                    ctx.getQueue().isDurable(),
                    ctx.getQueue().isExclusive(),
                    ctx.getQueue().isAutoDelete()
            );
            amqpAdmin.declareQueue(queue);

            Binding binding = switch (exchange) {
                case DirectExchange directExchange -> BindingBuilder.bind(queue).to(directExchange).with(routingKey);
                case TopicExchange topicExchange -> BindingBuilder.bind(queue).to(topicExchange).with(routingKey);
                case FanoutExchange fanoutExchange -> BindingBuilder.bind(queue).to(fanoutExchange);
                default -> throw new IllegalArgumentException("Unsupported exchange type: " + exchange.getType());
            };

            amqpAdmin.declareBinding(binding);
        }

        // Publish message
        amqpTemplate.convertAndSend(exchangeName, routingKey, ctx.getMessage());
    }
}