package com.automation.engine.http;

import com.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.automation.engine.provider.AEConfigProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

@Configuration
public class TestConfig {
    @Bean
    public AEConfigProvider automationEngineConfigProvider(ThreadPoolTaskScheduler taskScheduler) {
        return new AEConfigProvider()
                .setTaskScheduler(taskScheduler)
                .setDefaultTimeout(Duration.ofSeconds(1));
    }
}