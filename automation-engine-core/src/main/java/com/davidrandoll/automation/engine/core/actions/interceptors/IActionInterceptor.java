package com.davidrandoll.automation.engine.core.actions.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IActionInterceptor {
    void intercept(EventContext eventContext, ActionContext actionContext, IActionChain chain);
}