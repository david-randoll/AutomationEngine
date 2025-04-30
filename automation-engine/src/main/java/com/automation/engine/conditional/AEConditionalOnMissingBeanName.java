package com.automation.engine.conditional;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * This annotation is used to conditionally load a bean if a bean with the specified name does not exist.
 * It is a specialization of {@link AEConditionalOnMissingBean}.
 * <p>
 * The beanName is not required when used with {@link Component}, {@link Service}, or {@link Bean}.
 * It'll use the name of these annotations as the bean name.
 * </p>
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AEConditionalOnMissingBean
@interface AEConditionalOnMissingBeanName {
    @AliasFor(annotation = AEConditionalOnMissingBean.class, attribute = "name")
    String[] value() default {};
}