package com.davidrandoll.automation.engine.spring.events.properties;

import com.davidrandoll.automation.engine.utils.ConditionalUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AESpringEventsEnabled implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return ConditionalUtils.evaluate(context, "automation.engine.spring.events.enabled", "true", true);
    }
}
