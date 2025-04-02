package com.automation.engine.core.actions.interceptors;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.actions.IBaseAction;
import com.automation.engine.core.events.EventContext;
import org.springframework.core.OrderComparator;
import org.springframework.util.ObjectUtils;

import java.util.List;

public class InterceptingAction implements IAction {
    private final IBaseAction delegate;
    private final List<IActionInterceptor> interceptors;

    public InterceptingAction(IBaseAction delegate, List<IActionInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = ObjectUtils.isEmpty(interceptors) ? List.of() : interceptors.stream()
                .sorted(OrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public void execute(EventContext eventContext, ActionContext actionContext) {
        executeInterceptors(0, eventContext, actionContext);
    }

    private void executeInterceptors(int index, EventContext eventContext, ActionContext actionContext) {
        if (index < interceptors.size()) {
            IActionInterceptor interceptor = interceptors.get(index);
            IAction action = (ec, ac) -> this.executeInterceptors(index + 1, ec, ac);
            interceptor.intercept(eventContext, actionContext, action);
        } else {
            // All interceptors have been processed, execute the delegate
            delegate.execute(eventContext, actionContext);
        }
    }
}