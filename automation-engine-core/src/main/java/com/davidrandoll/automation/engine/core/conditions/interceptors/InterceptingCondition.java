package com.davidrandoll.automation.engine.core.conditions.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.List;
import java.util.Optional;

public class InterceptingCondition implements ICondition {
    private final ICondition delegate;
    private final List<IConditionInterceptor> interceptors;

    public InterceptingCondition(ICondition delegate, List<IConditionInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
    }

    @Override
    public boolean isSatisfied(EventContext eventContext, ConditionContext context) {
        IConditionChain chain = buildChain(0);
        return chain.isSatisfied(eventContext, context);
    }

    private IConditionChain buildChain(int index) {
        if (index >= interceptors.size()) {
            return new ConditionChain(this.delegate::isSatisfied, delegate);
        }

        IConditionInterceptor interceptor = interceptors.get(index);
        IConditionChain next = buildChain(index + 1);
        return new ConditionChain(
                (ec, cc) -> interceptor.intercept(ec, cc, next),
                delegate
        );
    }
}