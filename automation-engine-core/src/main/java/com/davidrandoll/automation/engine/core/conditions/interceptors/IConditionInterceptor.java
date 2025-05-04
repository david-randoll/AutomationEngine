package com.davidrandoll.automation.engine.core.conditions.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IConditionInterceptor {
    void intercept(EventContext eventContext, ConditionContext conditionContext, ICondition condition);
}