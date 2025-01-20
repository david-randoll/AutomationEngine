package com.automation.engine.engine.conditions;

import com.automation.engine.engine.events.EventContext;

public abstract class AbstractCondition implements ICondition {
    @Override
    public boolean isMet(EventContext context) {
        return false;
    }

    public abstract boolean isMet(EventContext context, ConditionContext conditionContext);
}