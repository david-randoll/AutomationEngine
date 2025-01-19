package com.automation.engine.conditions;

import com.automation.engine.events.EventContext;

@FunctionalInterface
public interface ICondition {
    boolean isMet(EventContext context);
}