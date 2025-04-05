package com.automation.engine.core.variables;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IVariable extends IBaseVariable {
    @Override
    default void resolve(EventContext context) {
        // used by FunctionalInterface to execute the execute method with VariableContext
    }

    default Class<?> getContextType() {
        return null;
    }

    @Override
    void resolve(EventContext context, VariableContext variableContext);
}