package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.core.IModule;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ITrigger extends IModule {
    boolean isTriggered(EventContext eventContext, TriggerContext triggerContext);
}