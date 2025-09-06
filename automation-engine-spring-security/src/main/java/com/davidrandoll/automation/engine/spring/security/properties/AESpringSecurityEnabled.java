package com.davidrandoll.automation.engine.spring.security.properties;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AESpringSecurityEnabled extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String property = context.getEnvironment().getProperty("automation.engine.spring.security.enabled");
        if (property == null || Boolean.parseBoolean(property)) {
            return ConditionOutcome.match("Automation Engine Spring Security is enabled");
        }
        return ConditionOutcome.noMatch("Automation Engine Spring Security is disabled");
    }
}