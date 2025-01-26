package com.automation.engine.core.conditions.interceptors;

import com.automation.engine.core.conditions.ConditionContext;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IConditionInterceptor {
    void intercept(Event event, ConditionContext context, ICondition condition);
}