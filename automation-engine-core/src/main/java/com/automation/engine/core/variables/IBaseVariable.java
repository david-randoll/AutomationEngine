package com.automation.engine.core.variables;

import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IBaseVariable {
    void setVariable(Event context);

    default void setVariable(Event context, VariableContext variableContext) {
        setVariable(context);
    }
}