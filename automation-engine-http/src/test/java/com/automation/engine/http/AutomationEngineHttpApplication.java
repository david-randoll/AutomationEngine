package com.automation.engine.http;

import com.automation.engine.AutomationEngineConfigProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class AutomationEngineHttpApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutomationEngineHttpApplication.class, args);
    }

    @Bean
    public AutomationEngineConfigProvider automationEngineConfigProvider() {
        return new AutomationEngineConfigProvider()
                .setDefaultTimeout(Duration.ofSeconds(1));
    }
}