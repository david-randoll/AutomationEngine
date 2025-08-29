package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ITrigger extends IBlock {
    boolean isTriggered(EventContext eventContext, TriggerContext triggerContext);
}