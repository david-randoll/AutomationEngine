package com.automation.engine.engine.core.conditions.interceptors;

import com.automation.engine.engine.core.conditions.ConditionContext;
import com.automation.engine.engine.core.conditions.ICondition;
import com.automation.engine.engine.core.events.EventContext;

@FunctionalInterface
public interface IConditionInterceptor {
    void intercept(EventContext context, ConditionContext conditionContext, ICondition condition);
}