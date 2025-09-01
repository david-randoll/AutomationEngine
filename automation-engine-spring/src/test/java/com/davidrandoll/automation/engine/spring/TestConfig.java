package com.davidrandoll.automation.engine.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

@Configuration
public class TestConfig {
    @Bean
    @Primary
    public AEConfigProvider automationEngineConfigProvider(ThreadPoolTaskScheduler taskScheduler) {
        return new AEConfigProvider()
                .setTaskScheduler(taskScheduler)
                .setDefaultTimeout(Duration.ofSeconds(1));
    }
}