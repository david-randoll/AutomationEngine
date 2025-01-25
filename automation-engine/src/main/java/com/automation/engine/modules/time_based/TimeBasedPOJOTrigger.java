package com.automation.engine.modules.time_based;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;
import org.springframework.lang.Nullable;

import java.time.LocalTime;

public class TimeBasedPOJOTrigger implements ITrigger {
    private final LocalTime beforeTime;
    private final LocalTime afterTime;

    public TimeBasedPOJOTrigger(@Nullable LocalTime beforeTime, @Nullable LocalTime afterTime) {
        this.beforeTime = beforeTime;
        this.afterTime = afterTime;
    }

    @Override
    public boolean isTriggered(Event event, TriggerContext triggerContext) {
        var result = false;
        if (event instanceof TimeBasedEvent timeBasedEvent) {
            LocalTime eventTime = timeBasedEvent.getTime();
            if (beforeTime == null && afterTime == null) return result;
            result = (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
        }
        return result;
    }
}