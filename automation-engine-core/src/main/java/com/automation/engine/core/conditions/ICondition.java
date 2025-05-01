package com.automation.engine.core.conditions;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ICondition {
    default Class<?> getContextType() {
        return null;
    }

    boolean isSatisfied(EventContext context, ConditionContext conditionContext);
}