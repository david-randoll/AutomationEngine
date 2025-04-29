package com.automation.engine.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Objects;

public class AEConditionalOnMissingBeanCondition implements Condition {
    @Override
    public boolean matches(@NonNull ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!metadata.isAnnotated(AEConditionalOnMissingBean.class.getName())) return true;

        Map<String, Object> attrs = metadata.getAnnotationAttributes(AEConditionalOnMissingBean.class.getName());
        if (attrs == null) return true;

        Class<?>[] types = (Class<?>[]) attrs.getOrDefault("value", new Class<?>[0]);

        for (Class<?> type : types) {
            if (Objects.requireNonNull(context.getBeanFactory()).getBeanNamesForType(type, true, false).length > 0) {
                return false;
            }
        }

        return true;
    }
}