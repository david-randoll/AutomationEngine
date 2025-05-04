package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IVariable {
    default Class<?> getContextType() {
        return null;
    }

    void resolve(EventContext context, VariableContext variableContext);
}