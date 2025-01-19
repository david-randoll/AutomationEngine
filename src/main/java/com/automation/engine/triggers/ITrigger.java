package com.automation.engine.triggers;

import com.automation.engine.events.Event;

@FunctionalInterface
public interface ITrigger {
    boolean isTriggered(Event event);
}
