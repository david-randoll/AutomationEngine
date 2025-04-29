package com.automation.engine.conditional;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Objects;

class AEConditionalOnMissingBeanCondition implements Condition {
    @Override
    public boolean matches(@NonNull ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!metadata.isAnnotated(AEConditionalOnMissingBean.class.getName())) return true;

        Map<String, Object> attrs = metadata.getAnnotationAttributes(AEConditionalOnMissingBean.class.getName());
        if (attrs == null) return true;

        Class<?>[] types = (Class<?>[]) attrs.getOrDefault("type", new Class<?>[0]);
        String[] beanNames = (String[]) attrs.getOrDefault("beanNames", new String[0]);

        ConfigurableListableBeanFactory beanFactory = Objects.requireNonNull(context.getBeanFactory());

        for (Class<?> type : types) {
            if (beanFactory.getBeanNamesForType(type, true, false).length > 0) {
                return false;
            }
        }

        for (String name : beanNames) {
            if (beanFactory.containsBeanDefinition(name) || beanFactory.containsSingleton(name)) {
                return false;
            }
        }

        return true;
    }
}