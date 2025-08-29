package com.davidrandoll.automation.engine.spring.events;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event.PublishSpringEventAction;
import com.davidrandoll.automation.engine.spring.events.properties.AESpringEventsEnabled;
import com.davidrandoll.automation.engine.spring.events.properties.AESpringEventsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(AESpringEventsEnabled.class)
@ConditionalOnClass(ApplicationEventPublisher.class)
public class AESpringEventsConfig {
    @Bean("publishSpringEventAction")
    @ConditionalOnMissingBean(name = "publishSpringEventAction", ignored = PublishSpringEventAction.class)
    public PublishSpringEventAction publishSpringEventAction(AutomationEngine engine, ApplicationEventPublisher publisher, ObjectMapper mapper) {
        return new PublishSpringEventAction(engine, publisher, mapper);
    }

    @Bean
    @ConfigurationProperties(prefix = "automation.engine.spring.events")
    public AESpringEventsProperties properties() {
        return new AESpringEventsProperties();
    }
}
