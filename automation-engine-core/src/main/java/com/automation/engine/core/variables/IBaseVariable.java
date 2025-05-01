package com.automation.engine.core.variables;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseVariable {
    void resolve(EventContext context);
}