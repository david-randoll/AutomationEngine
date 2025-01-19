package com.automation.engine.core.triggers;

import com.automation.engine.core.events.Event;

@FunctionalInterface
public interface ITrigger {
    boolean isTriggered(Event event);

    default boolean isTriggered(Event event, TriggerContext triggerContext) {
        return isTriggered(event);
    }
}