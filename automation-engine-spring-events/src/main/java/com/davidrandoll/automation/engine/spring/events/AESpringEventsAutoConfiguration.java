package com.davidrandoll.automation.engine.spring.events;

import com.davidrandoll.automation.engine.AutomationEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AESpringEventsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SpringEventConsumer springEventConsumer(AutomationEngine automationEngine) {
        return new SpringEventConsumer(automationEngine);
    }
}
