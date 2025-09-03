package com.davidrandoll.automation.engine.spring.amqp;

import com.davidrandoll.automation.engine.spring.amqp.actions.PublishRabbitMqEventAction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AEAmqpConfig {
    @Bean("publishRabbitMqEventAction")
    @ConditionalOnMissingBean(name = "publishRabbitMqEventAction", ignored = PublishRabbitMqEventAction.class)
    public PublishRabbitMqEventAction publishRabbitMqEventAction() {
        return new PublishRabbitMqEventAction();
    }
}
