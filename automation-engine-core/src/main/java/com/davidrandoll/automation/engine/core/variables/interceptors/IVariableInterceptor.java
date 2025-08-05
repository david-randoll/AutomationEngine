package com.davidrandoll.automation.engine.core.variables.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.VariableContext;

@FunctionalInterface
public interface IVariableInterceptor {
    void intercept(EventContext eventContext, VariableContext variableContext, IVariableChain chain);
}