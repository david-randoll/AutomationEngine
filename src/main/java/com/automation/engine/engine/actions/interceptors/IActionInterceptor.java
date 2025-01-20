package com.automation.engine.engine.actions.interceptors;

import com.automation.engine.engine.actions.ActionContext;
import com.automation.engine.engine.actions.IAction;
import com.automation.engine.engine.events.EventContext;

@FunctionalInterface
public interface IActionInterceptor {
    void intercept(EventContext context, ActionContext actionContext, IAction action);
}