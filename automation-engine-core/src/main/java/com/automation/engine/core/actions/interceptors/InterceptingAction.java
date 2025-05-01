package com.automation.engine.core.actions.interceptors;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.events.EventContext;

import java.util.List;
import java.util.Optional;

public class InterceptingAction implements IAction {
    private final IAction delegate;
    private final List<IActionInterceptor> interceptors;

    public InterceptingAction(IAction delegate, List<IActionInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
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