package com.davidrandoll.automation.engine.spring.amqp;

import com.davidrandoll.automation.engine.spring.amqp.actions.PublishRabbitMqEventAction;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({AmqpTemplate.class, AmqpAdmin.class})
public class AEAmqpConfig {
    @Bean("publishRabbitMqEventAction")
    @ConditionalOnMissingBean(name = "publishRabbitMqEventAction", ignored = PublishRabbitMqEventAction.class)
    public PublishRabbitMqEventAction publishRabbitMqEventAction(AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        return new PublishRabbitMqEventAction(amqpTemplate, amqpAdmin);
    }
}
