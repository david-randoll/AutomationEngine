package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ITrigger {
    default Class<?> getContextType() {
        return null;
    }

    boolean isTriggered(EventContext eventContext, TriggerContext triggerContext);

    default boolean autoEvaluateExpression() {
        return true;
    }
}