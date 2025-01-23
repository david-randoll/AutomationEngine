package com.automation.engine.engine.conditions;

import com.automation.engine.engine.events.EventContext;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ICondition extends IBaseCondition {
    @Override
    default boolean isMet(EventContext context) {
        return false;
    }

    @Nullable
    default Class<?> getContextType() {
        return null;
    }

    @Override
    boolean isMet(EventContext context, ConditionContext conditionContext);
}