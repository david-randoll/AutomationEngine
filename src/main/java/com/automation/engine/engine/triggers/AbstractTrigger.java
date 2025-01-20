package com.automation.engine.engine.triggers;

import com.automation.engine.engine.events.Event;

public abstract class AbstractTrigger implements ITrigger {
    @Override
    public boolean isTriggered(Event event) {
        return false;
    }

    public abstract boolean isTriggered(Event event, TriggerContext triggerContext);
}