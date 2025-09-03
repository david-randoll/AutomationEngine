package com.davidrandoll.automation.engine.spring.amqp;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AEAmqpConfig.class)
public class AEAmqpAutoConfiguration {
}