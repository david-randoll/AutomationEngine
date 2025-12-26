package com.davidrandoll.automation.engine;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.davidrandoll.automation.engine.actions")
public class TestConfig {
}
