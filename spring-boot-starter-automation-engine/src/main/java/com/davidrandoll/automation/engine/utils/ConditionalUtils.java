package com.davidrandoll.automation.engine.utils;

import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.ConditionContext;

@UtilityClass
public class ConditionalUtils {
    /**
     * This will mimic the @ConditionalOnProperty annotation.
     *
     * @param context        the condition context
     * @param propertyName   the name of the property to check
     * @param havingValue    the value that the property should have
     * @param matchIfMissing if true, will return true if the property is missing, otherwise false
     * @return true if the property matches the havingValue, false otherwise
     */
    public static boolean evaluate(ConditionContext context, String propertyName, String havingValue, boolean matchIfMissing) {
        String value = context.getEnvironment().getProperty(propertyName);
        if (value == null) {
            return matchIfMissing;
        }
        return value.equalsIgnoreCase(havingValue);
    }
}
