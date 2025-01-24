package com.automation.engine.engine.core.actions.interceptors;

import com.automation.engine.engine.core.actions.IAction;
import com.automation.engine.engine.core.actions.ActionContext;
import com.automation.engine.engine.core.events.EventContext;

@FunctionalInterface
public interface IActionInterceptor {
    void intercept(EventContext context, ActionContext actionContext, IAction action);
}