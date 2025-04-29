package com.automation.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class AutomationEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutomationEngineApplication.class, args);
    }

    @Bean
    public AEConfigProvider automationEngineConfigProvider() {
        return new AEConfigProvider()
                .setDefaultTimeout(Duration.ofSeconds(1));
    }
}