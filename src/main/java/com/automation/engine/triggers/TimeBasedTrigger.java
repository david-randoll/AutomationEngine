package com.automation.engine.triggers;

import com.automation.engine.events.Event;
import com.automation.engine.events.TimeEvent;
import org.springframework.lang.Nullable;

import java.time.LocalTime;

public class TimeBasedTrigger implements ITrigger {
    private final LocalTime beforeTime;
    private final LocalTime afterTime;

    public TimeBasedTrigger(@Nullable LocalTime beforeTime, @Nullable LocalTime afterTime) {
        this.beforeTime = beforeTime;
        this.afterTime = afterTime;
    }

    @Override
    public boolean isTriggered(Event event) {
        var result = false;
        if (event instanceof TimeEvent timeEvent) {
            LocalTime eventTime = timeEvent.getTime();
            if (beforeTime == null && afterTime == null) return result;
            result = (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
        }
        return result;
    }
}