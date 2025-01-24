package com.automation.engine.engine.core.triggers.interceptors;

import com.automation.engine.engine.core.events.Event;
import com.automation.engine.engine.core.triggers.ITrigger;
import com.automation.engine.engine.core.triggers.TriggerContext;

@FunctionalInterface
public interface ITriggerInterceptor {
    void intercept(Event event, TriggerContext triggerContext, ITrigger trigger);
}