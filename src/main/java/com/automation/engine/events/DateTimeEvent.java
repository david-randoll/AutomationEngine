package com.automation.engine.events;

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