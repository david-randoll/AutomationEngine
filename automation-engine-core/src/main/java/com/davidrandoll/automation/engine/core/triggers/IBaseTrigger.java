package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseTrigger {
    boolean isTriggered(EventContext eventContext);
}