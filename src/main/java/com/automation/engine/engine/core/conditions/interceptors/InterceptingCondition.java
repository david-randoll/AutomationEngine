package com.automation.engine.engine.core.conditions.interceptors;

import com.automation.engine.engine.core.conditions.ConditionContext;
import com.automation.engine.engine.core.conditions.IBaseCondition;
import com.automation.engine.engine.core.conditions.ICondition;
import com.automation.engine.engine.core.events.EventContext;
import org.springframework.core.OrderComparator;
import org.springframework.util.ObjectUtils;

import java.util.List;

public class InterceptingCondition implements ICondition {
    private final IBaseCondition delegate;
    private final List<IConditionInterceptor> interceptors;

    public InterceptingCondition(IBaseCondition delegate, List<IConditionInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = ObjectUtils.isEmpty(interceptors) ? List.of() : interceptors.stream()
                .sorted(OrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public boolean isMet(EventContext context, ConditionContext conditionContext) {
        return executeInterceptors(0, context, conditionContext);
    }

    private boolean executeInterceptors(int index, EventContext context, ConditionContext conditionContext) {
        if (index < interceptors.size()) {
            IConditionInterceptor interceptor = interceptors.get(index);
            ICondition action = (ec, cc) -> this.executeInterceptors(index + 1, ec, cc);
            interceptor.intercept(context, conditionContext, action);
            return true; // doesn't matter what the interceptor returns, the delegate will return the final result
        } else {
            // All interceptors have been processed, execute the delegate
            return delegate.isMet(context, conditionContext);
        }
    }
}