package com.davidrandoll.automation.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestConfig.class)
public class AEStarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(AEStarterApplication.class, args);
    }
}