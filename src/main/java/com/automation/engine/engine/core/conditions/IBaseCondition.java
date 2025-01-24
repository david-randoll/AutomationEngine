package com.automation.engine.engine.core.conditions;

import com.automation.engine.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseCondition {
    boolean isMet(EventContext context);

    default boolean isMet(EventContext context, ConditionContext conditionContext) {
        return isMet(context);
    }
}