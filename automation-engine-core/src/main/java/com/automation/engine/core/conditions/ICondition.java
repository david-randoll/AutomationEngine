package com.automation.engine.core.conditions;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ICondition extends IBaseCondition {
    @Override
    default boolean isSatisfied(EventContext context) {
        return false;
    }

    default Class<?> getContextType() {
        return null;
    }

    @Override
    boolean isSatisfied(EventContext context, ConditionContext conditionContext);
}