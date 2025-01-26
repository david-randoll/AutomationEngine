package com.automation.engine.core.actions.interceptors;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface IActionInterceptor {
    void intercept(Event event, ActionContext context, IAction action);
}