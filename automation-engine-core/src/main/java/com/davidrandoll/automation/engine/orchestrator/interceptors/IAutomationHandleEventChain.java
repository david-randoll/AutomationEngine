package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IAutomationHandleEventChain {
    void proceed(EventContext eventContext);
}