package com.automation.engine.engine.conditions;

import com.automation.engine.engine.events.EventContext;

@FunctionalInterface
public interface ICondition extends IBaseCondition {
    @Override
    default boolean isMet(EventContext context) {
        return false;
    }

    @Override
    boolean isMet(EventContext context, ConditionContext conditionContext);
}