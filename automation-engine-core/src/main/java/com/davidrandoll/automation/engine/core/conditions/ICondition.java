package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.core.IModule;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ICondition extends IModule {
    boolean isSatisfied(EventContext context, ConditionContext conditionContext);
}