package com.davidrandoll.automation.engine.core.conditions.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ConditionChain implements IConditionChain {
    private final IConditionChain delegate;
    private final ICondition condition;

    @Override
    public boolean isSatisfied(EventContext context, ConditionContext conditionContext) {
        return delegate.isSatisfied(context, conditionContext);
    }

    @Override
    public Class<?> getContextType() {
        return condition.getContextType();
    }

    @Override
    public boolean autoEvaluateExpression() {
        return condition.autoEvaluateExpression();
    }
}