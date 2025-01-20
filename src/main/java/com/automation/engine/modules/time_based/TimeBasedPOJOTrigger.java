package com.automation.engine.modules.time_based;

import com.automation.engine.engine.events.Event;
import com.automation.engine.engine.triggers.AbstractTrigger;
import com.automation.engine.engine.triggers.TriggerContext;
import org.springframework.lang.Nullable;

import java.time.LocalTime;

public class TimeBasedPOJOTrigger extends AbstractTrigger {
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