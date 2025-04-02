package com.automation.engine.core.actions.interceptors;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IActionInterceptor {
    void intercept(EventContext eventContext, ActionContext actionContext, IAction action);
}