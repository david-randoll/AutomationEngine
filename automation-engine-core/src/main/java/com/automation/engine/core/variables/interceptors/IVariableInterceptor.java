package com.automation.engine.core.variables.interceptors;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.VariableContext;

@FunctionalInterface
public interface IVariableInterceptor {
    void intercept(EventContext eventContext, VariableContext variableContext, IVariable variable);
}