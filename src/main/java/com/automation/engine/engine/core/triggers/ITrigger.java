package com.automation.engine.engine.core.triggers;

import com.automation.engine.engine.core.events.Event;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ITrigger extends IBaseTrigger {
    @Override
    default boolean isTriggered(Event event) {
        return false;
    }

    @Nullable
    default Class<?> getContextType() {
        return null;
    }

    @Override
    boolean isTriggered(Event event, TriggerContext triggerContext);
}