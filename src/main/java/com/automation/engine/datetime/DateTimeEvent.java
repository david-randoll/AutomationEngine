package com.automation.engine.datetime;

import com.automation.engine.core.events.Event;

import java.time.LocalDateTime;
import java.util.Map;

public class DateTimeEvent extends Event {
    public DateTimeEvent(LocalDateTime dateTime) {
        super("DateTimeEvent", Map.of("dateTime", dateTime));
    }

    public LocalDateTime getDateTime() {
        return (LocalDateTime) getData().get("dateTime");
    }
}