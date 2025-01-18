package com.automation.engine.triggers;

import com.automation.engine.events.Event;

public interface ITrigger {
    boolean isTriggered(Event event);
}
