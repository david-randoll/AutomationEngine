package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.core.IModule;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IVariable extends IModule {
    void resolve(EventContext context, VariableContext variableContext);
}