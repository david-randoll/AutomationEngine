package com.davidrandoll.automation.engine.core.variables.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.core.variables.VariableContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VariableChain implements IVariableChain {
    private final IVariableChain delegate;
    private final IVariable variable;

    @Override
    public Class<?> getContextType() {
        return variable.getContextType();
    }

    @Override
    public void resolve(EventContext context, VariableContext variableContext) {
        delegate.resolve(context, variableContext);
    }

    @Override
    public boolean autoEvaluateExpression() {
        return variable.autoEvaluateExpression();
    }
}
