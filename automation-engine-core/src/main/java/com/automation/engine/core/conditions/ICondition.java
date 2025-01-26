package com.automation.engine.core.conditions;

import com.automation.engine.core.events.Event;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ICondition extends IBaseCondition {
    @Override
    default boolean isSatisfied(Event context) {
        return false;
    }

    @Nullable
    default Class<?> getContextType() {
        return null;
    }

    @Override
    boolean isSatisfied(Event context, ConditionContext conditionContext);
}