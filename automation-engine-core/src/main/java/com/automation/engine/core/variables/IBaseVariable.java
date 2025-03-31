package com.automation.engine.core.variables;

import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IBaseVariable {
    void resolve(Event context);

    default void resolve(Event context, VariableContext variableContext) {
        resolve(context);
    }
}