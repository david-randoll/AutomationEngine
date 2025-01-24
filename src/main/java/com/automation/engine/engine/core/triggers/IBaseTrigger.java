package com.automation.engine.engine.core.triggers;

import com.automation.engine.engine.core.events.Event;

@FunctionalInterface
public interface IBaseTrigger {
    boolean isTriggered(Event event);

    default boolean isTriggered(Event event, TriggerContext triggerContext) {
        return isTriggered(event);
    }
}