package com.davidrandoll.automation.engine.core.actions.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.events.EventContext;

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
        IActionChain chain = buildChain(0);
        chain.execute(eventContext, new ActionContext(actionContext));
    }

    private IActionChain buildChain(int index) {
        if (index >= interceptors.size()) {
            return new ActionChain(this.delegate::execute, delegate);
        }

        IActionInterceptor interceptor = interceptors.get(index);
        IActionChain next = buildChain(index + 1);
        return new ActionChain(
                (ec, ac) -> interceptor.intercept(ec, ac, next),
                delegate
        );
    }
}