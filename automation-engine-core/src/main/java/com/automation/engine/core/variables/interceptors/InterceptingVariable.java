package com.automation.engine.core.variables.interceptors;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.variables.IBaseVariable;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.VariableContext;

import java.util.List;
import java.util.Optional;

public class InterceptingVariable implements IVariable {
    private final IBaseVariable delegate;
    private final List<IVariableInterceptor> interceptors;

    public InterceptingVariable(IBaseVariable delegate, List<IVariableInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
    }

    @Override
    public void resolve(EventContext eventContext, VariableContext variableContext) {
        executeInterceptors(0, eventContext, variableContext);
    }

    private void executeInterceptors(int index, EventContext eventContext, VariableContext variableContext) {
        if (index < interceptors.size()) {
            IVariableInterceptor interceptor = interceptors.get(index);
            IVariable action = (ec, ac) -> this.executeInterceptors(index + 1, ec, ac);
            interceptor.intercept(eventContext, variableContext, action);
        } else {
            // All interceptors have been processed, execute the delegate
            delegate.resolve(eventContext, variableContext);
        }
    }
}