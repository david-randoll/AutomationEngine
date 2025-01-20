package com.automation.engine.engine.triggers.interceptors;

import com.automation.engine.engine.events.Event;
import com.automation.engine.engine.triggers.ITrigger;
import com.automation.engine.engine.triggers.TriggerContext;

@FunctionalInterface
public interface ITriggerInterceptor {
    void intercept(Event event, TriggerContext triggerContext, ITrigger trigger);
}