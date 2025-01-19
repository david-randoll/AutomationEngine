package com.automation.engine.datetime;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.ITrigger;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public class DateTimeTrigger implements ITrigger {
    private final LocalDateTime beforeTime;
    private final LocalDateTime afterTime;

    public DateTimeTrigger(@Nullable LocalDateTime beforeTime, @Nullable LocalDateTime afterTime) {
        this.beforeTime = beforeTime;
        this.afterTime = afterTime;
    }

    @Override
    public boolean isTriggered(Event event) {
        if (event instanceof DateTimeEvent dateTimeEvent) {
            LocalDateTime eventTime = dateTimeEvent.getDateTime();
            if (eventTime == null) return false;
            if (beforeTime == null && afterTime == null) return false;
            return (beforeTime == null || eventTime.isAfter(beforeTime)) && (afterTime == null || eventTime.isBefore(afterTime));
        } else {
            return false;
        }
    }
}