package com.davidrandoll.automation.engine.backend.api.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestConfig.class)
public class AEBackendApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AEBackendApiApplication.class, args);
    }
}