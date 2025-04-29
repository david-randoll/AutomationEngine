package com.automation.engine.conditional;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AEConditionalOnMissingBean
public @interface AEConditionalOnMissingBeanName {
    @AliasFor(annotation = AEConditionalOnMissingBean.class, attribute = "beanNames")
    String[] value();
}