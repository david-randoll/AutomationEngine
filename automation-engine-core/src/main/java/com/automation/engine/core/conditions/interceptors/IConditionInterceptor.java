package com.automation.engine.core.conditions.interceptors;

import com.automation.engine.core.conditions.ConditionContext;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IConditionInterceptor {
    void intercept(EventContext eventContext, ConditionContext conditionContext, ICondition condition);
}