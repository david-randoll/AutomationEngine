package com.automation.engine.engine.triggers;

import com.automation.engine.engine.events.Event;

@FunctionalInterface
public interface ITrigger extends IBaseTrigger {
    @Override
    default boolean isTriggered(Event event) {
        return false;
    }

    boolean isTriggered(Event event, TriggerContext triggerContext);
}