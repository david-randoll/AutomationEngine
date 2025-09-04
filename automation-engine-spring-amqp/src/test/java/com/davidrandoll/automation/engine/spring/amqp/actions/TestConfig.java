package com.davidrandoll.automation.engine.spring.amqp.actions;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    public AmqpAdmin amqpAdmin() {
        return mock(AmqpAdmin.class);
    }

    @Bean
    public AmqpTemplate amqpTemplate() {
        return mock(AmqpTemplate.class);
    }
}
