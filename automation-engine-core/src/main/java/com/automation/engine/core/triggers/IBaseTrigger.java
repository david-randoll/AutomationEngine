package com.automation.engine.core.triggers;

import com.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface IBaseTrigger {
    boolean isTriggered(EventContext eventContext);
}