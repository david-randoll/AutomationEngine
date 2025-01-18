package com.automation.engine.conditions;

import com.automation.engine.events.EventContext;

public interface ICondition {
    boolean isMet(EventContext context);
}