package com.automation.engine.core.conditions;

import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IBaseCondition {
    boolean isSatisfied(Event context);

    default boolean isSatisfied(Event context, ConditionContext conditionContext) {
        return isSatisfied(context);
    }
}