package com.automation.engine.engine.actions.interceptors;

import com.automation.engine.engine.actions.ActionContext;
import com.automation.engine.engine.actions.ActionExecutor;
import com.automation.engine.engine.actions.IAction;
import com.automation.engine.engine.events.EventContext;
import org.springframework.core.OrderComparator;
import org.springframework.util.ObjectUtils;

import java.util.List;

public class InterceptingAction implements ActionExecutor {
    private final IAction delegate;
    private final List<IActionInterceptor> interceptors;

    public InterceptingAction(IAction delegate, List<IActionInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = ObjectUtils.isEmpty(interceptors) ? List.of() : interceptors.stream()
                .sorted(OrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public void execute(EventContext context, ActionContext actionContext) {
        executeInterceptors(0, context, actionContext);
    }

    private void executeInterceptors(int index, EventContext context, ActionContext actionContext) {
        if (index < interceptors.size()) {
            IActionInterceptor interceptor = interceptors.get(index);
            ActionExecutor action = (ec, ac) -> this.executeInterceptors(index + 1, ec, ac);
            interceptor.intercept(context, actionContext, action);
        } else {
            // All interceptors have been processed, execute the delegate
            delegate.execute(context, actionContext);
        }
    }
}