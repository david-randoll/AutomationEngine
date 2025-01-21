package com.automation.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutomationEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomationEngineApplication.class, args);
    }
}