package com.automation.engine.core.variables.interceptors;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.VariableContext;

@FunctionalInterface
public interface IVariableInterceptor {
    void intercept(Event event, VariableContext context, IVariable variable);
}