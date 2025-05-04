package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseVariable {
    void resolve(EventContext context);
}