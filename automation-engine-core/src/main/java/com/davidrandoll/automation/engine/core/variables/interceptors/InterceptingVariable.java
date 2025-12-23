package com.davidrandoll.automation.engine.core.variables.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.core.variables.VariableContext;

import java.util.List;
import java.util.Optional;

public class InterceptingVariable implements IVariable {
    private final IVariable delegate;
    private final List<IVariableInterceptor> interceptors;

    public InterceptingVariable(IVariable delegate, List<IVariableInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
    }

    @Override
    public void resolve(EventContext eventContext, VariableContext variableContext) {
        IVariableChain chain = buildChain(0);
        chain.resolve(eventContext, new VariableContext(variableContext));
    }

    private IVariableChain buildChain(int index) {
        if (index >= interceptors.size()) {
            return new VariableChain(this.delegate::resolve, delegate);
        }

        IVariableInterceptor interceptor = interceptors.get(index);
        IVariableChain next = buildChain(index + 1);
        return new VariableChain(
                (ec, vc) -> interceptor.intercept(ec, vc, next),
                delegate
        );
    }
}