package com.davidrandoll.automation.engine.core.triggers.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;

@FunctionalInterface
public interface ITriggerChain {
    boolean isTriggered(EventContext eventContext, TriggerContext triggerContext);
}