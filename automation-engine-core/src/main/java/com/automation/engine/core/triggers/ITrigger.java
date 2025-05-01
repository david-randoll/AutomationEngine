package com.automation.engine.core.triggers;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ITrigger {
    default Class<?> getContextType() {
        return null;
    }

    boolean isTriggered(EventContext eventContext, TriggerContext triggerContext);
}