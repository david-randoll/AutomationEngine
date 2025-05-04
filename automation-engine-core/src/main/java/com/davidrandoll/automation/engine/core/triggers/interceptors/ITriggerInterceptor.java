package com.davidrandoll.automation.engine.core.triggers.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;

@FunctionalInterface
public interface ITriggerInterceptor {
    void intercept(EventContext eventContext, TriggerContext triggerContext, ITrigger trigger);
}