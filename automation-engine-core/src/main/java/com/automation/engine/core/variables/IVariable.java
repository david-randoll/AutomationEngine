package com.automation.engine.core.variables;

import com.automation.engine.core.events.Event;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface IVariable extends IBaseVariable {
    @Override
    default void resolve(Event context) {
        // used by FunctionalInterface to execute the execute method with VariableContext
    }

    @Nullable
    default Class<?> getContextType() {
        return null;
    }

    @Override
    void resolve(Event context, VariableContext variableContext);
}