package com.automation.engine.core.variables;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IVariable {
    default Class<?> getContextType() {
        return null;
    }

    void resolve(EventContext context, VariableContext variableContext);
}