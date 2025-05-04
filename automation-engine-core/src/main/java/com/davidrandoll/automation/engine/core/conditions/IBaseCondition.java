package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseCondition {
    boolean isSatisfied(EventContext context);
}