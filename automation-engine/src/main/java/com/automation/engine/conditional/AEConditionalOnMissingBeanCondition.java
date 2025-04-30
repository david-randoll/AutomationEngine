package com.automation.engine.conditional;

import com.automation.engine.AEConfigProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;

class AEConditionalOnMissingBeanCondition implements Condition {
    @Override
    public boolean matches(@NonNull ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!metadata.isAnnotated(AEConditionalOnMissingBean.class.getName())) return true;

        Map<String, Object> attrs = metadata.getAnnotationAttributes(AEConditionalOnMissingBean.class.getName());
        if (attrs == null) return true;

        Class<?>[] types = (Class<?>[]) attrs.getOrDefault("type", new Class<?>[0]);
        var names = this.getBeanNames(metadata);

        if (Arrays.stream(types).noneMatch(x -> x.equals(AEConfigProvider.class))) return true;

        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory == null) return true;

        for (Class<?> type : types) {
            if (beanFactory.getBeanNamesForType(type).length > 0) {
                return false;
            }
        }

        for (String name : names) {
            if (beanFactory.containsBeanDefinition(name) || beanFactory.containsSingleton(name)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the names from AEConditionalOnMissingBean if present.
     * or from @Component if present.
     * or from @Service if present.
     * or from @Bean if present.
     */
    TreeSet<String> getBeanNames(AnnotatedTypeMetadata metadata) {
        var names = new TreeSet<String>();
        addToBeanNames(metadata, AEConditionalOnMissingBean.class, "name", names);
        addToBeanNames(metadata, Component.class, "value", names);
        addToBeanNames(metadata, Service.class, "value", names);
        addToBeanNames(metadata, Bean.class, "name", names);
        return names;
    }

    private static void addToBeanNames(AnnotatedTypeMetadata metadata, Class<? extends Annotation> annotation, String value, TreeSet<String> names) {
        Map<String, Object> beanNames = metadata.getAnnotationAttributes(annotation.getName());
        if (beanNames != null) {
            var nameAttr = beanNames.get(value);
            if (ObjectUtils.isEmpty(nameAttr)) return;
            switch (nameAttr) {
                case String str -> names.add(str);
                case String[] arr -> Collections.addAll(names, arr);
                default ->
                        throw new IllegalArgumentException("Invalid value for " + value + " in " + annotation.getName());
            }
        }
    }
}