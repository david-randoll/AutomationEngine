package com.automation.engine.engine.conditions.interceptors;

import com.automation.engine.engine.conditions.ConditionContext;
import com.automation.engine.engine.conditions.ICondition;
import com.automation.engine.engine.events.EventContext;

@FunctionalInterface
public interface IConditionInterceptor {
    void intercept(EventContext context, ConditionContext conditionContext, ICondition condition);
}