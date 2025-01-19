package com.automation.engine.engine.conditions;

import com.automation.engine.engine.events.EventContext;

@FunctionalInterface
public interface ICondition {
    boolean isMet(EventContext context);

    default boolean isMet(EventContext context, ConditionContext conditionContext) {
        return isMet(context);
    }
}