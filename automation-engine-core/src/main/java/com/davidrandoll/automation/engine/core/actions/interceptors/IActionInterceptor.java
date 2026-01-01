package com.davidrandoll.automation.engine.core.actions.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IActionInterceptor {
    ActionResult intercept(EventContext eventContext, ActionContext actionContext, IActionChain chain);
}