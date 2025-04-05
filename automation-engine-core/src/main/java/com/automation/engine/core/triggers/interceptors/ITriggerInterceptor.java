package com.automation.engine.core.triggers.interceptors;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;

@FunctionalInterface
public interface ITriggerInterceptor {
    void intercept(EventContext eventContext, TriggerContext triggerContext, ITrigger trigger);
}