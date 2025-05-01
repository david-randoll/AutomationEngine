package com.automation.engine.core.conditions;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseCondition {
    boolean isSatisfied(EventContext context);
}