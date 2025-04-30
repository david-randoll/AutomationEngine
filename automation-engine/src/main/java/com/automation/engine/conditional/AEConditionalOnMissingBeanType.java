package com.automation.engine.conditional;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to conditionally load a bean if a bean with the specified type does not exist.
 * It is a specialization of {@link AEConditionalOnMissingBean}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AEConditionalOnMissingBean
@interface AEConditionalOnMissingBeanType {
    @AliasFor(annotation = AEConditionalOnMissingBean.class, attribute = "type")
    Class<?>[] value();
}