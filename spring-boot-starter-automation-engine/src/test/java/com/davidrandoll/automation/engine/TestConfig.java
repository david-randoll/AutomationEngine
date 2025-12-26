package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.actions.ObjectTypeTestAction;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean("objectTypeTestAction")
    public ObjectTypeTestAction objectTypeTestAction() {
        return new ObjectTypeTestAction();
    }
}