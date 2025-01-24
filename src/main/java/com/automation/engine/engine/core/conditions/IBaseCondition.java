package com.automation.engine.engine.core.conditions;

import com.automation.engine.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseCondition {
    boolean isSatisfied(EventContext context);

    default boolean isSatisfied(EventContext context, ConditionContext conditionContext) {
        return isSatisfied(context);
    }
}