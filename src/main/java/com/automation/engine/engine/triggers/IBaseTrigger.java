package com.automation.engine.engine.triggers;

import com.automation.engine.engine.events.Event;

@FunctionalInterface
public interface IBaseTrigger {
    boolean isTriggered(Event event);

    default boolean isTriggered(Event event, TriggerContext triggerContext) {
        return isTriggered(event);
    }
}