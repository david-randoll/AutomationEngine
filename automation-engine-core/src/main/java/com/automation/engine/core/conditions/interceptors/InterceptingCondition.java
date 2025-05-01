package com.automation.engine.core.conditions.interceptors;

import com.automation.engine.core.conditions.ConditionContext;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.core.events.EventContext;

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
        return executeInterceptors(0, eventContext, context);
    }

    private boolean executeInterceptors(int index, EventContext eventContext, ConditionContext context) {
        if (index < interceptors.size()) {
            IConditionInterceptor interceptor = interceptors.get(index);
            final boolean[] resultHolder = {false};
            ICondition condition = (ec, cc) -> resultHolder[0] = this.executeInterceptors(index + 1, ec, cc);
            interceptor.intercept(eventContext, context, condition);
            return resultHolder[0];
        } else {
            // All interceptors processed, execute the delegate
            return delegate.isSatisfied(eventContext, context);
        }
    }
}