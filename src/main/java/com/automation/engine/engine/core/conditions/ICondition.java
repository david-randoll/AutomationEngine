package com.automation.engine.engine.core.conditions;

import com.automation.engine.engine.core.events.EventContext;
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