package com.automation.engine.core.triggers;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ITrigger extends IBaseTrigger {
    @Override
    default boolean isTriggered(EventContext eventContext) {
        return false;
    }

    default Class<?> getContextType() {
        return null;
    }

    @Override
    boolean isTriggered(EventContext eventContext, TriggerContext triggerContext);
}