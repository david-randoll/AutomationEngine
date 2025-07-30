package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;

public interface IAutomationHandleEventInterceptor {
    void intercept(EventContext eventContext, IAutomationHandleEventChain chain);
}