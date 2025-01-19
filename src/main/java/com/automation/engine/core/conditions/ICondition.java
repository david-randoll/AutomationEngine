package com.automation.engine.core.conditions;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ICondition {
    boolean isMet(EventContext context);

    default boolean isMet(EventContext context, ConditionContext conditionContext) {
        return isMet(context);
    }
}