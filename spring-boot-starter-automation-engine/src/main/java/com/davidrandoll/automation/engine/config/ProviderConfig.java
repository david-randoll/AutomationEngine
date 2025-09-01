package com.davidrandoll.automation.engine.config;

import com.davidrandoll.automation.engine.AEConfigProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ProviderConfig {

    @Bean
    @ConditionalOnMissingBean
    public AEConfigProvider automationEngineConfigProvider(ThreadPoolTaskScheduler taskScheduler) {
        return new AEConfigProvider()
                .setTaskScheduler(taskScheduler);
    }
}