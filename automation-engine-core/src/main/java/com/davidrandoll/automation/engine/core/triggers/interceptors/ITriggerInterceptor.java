package com.davidrandoll.automation.engine.core.triggers.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;

@FunctionalInterface
public interface ITriggerInterceptor {
    boolean intercept(EventContext eventContext, TriggerContext triggerContext, ITriggerChain trigger);
}