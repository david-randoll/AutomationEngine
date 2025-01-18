package com.automation.engine.events;

import org.springframework.lang.NonNull;

import java.time.LocalTime;
import java.util.Map;

public class TimeEvent extends Event {
    public static final String EVENT_NAME = "TimeEvent";
    private static final String FIELD_NAME = "time";

    public TimeEvent(@NonNull LocalTime dateTime) {
        super(EVENT_NAME, Map.of(FIELD_NAME, dateTime));
    }

    @NonNull
    public LocalTime getTime() {
        return (LocalTime) getData().get(FIELD_NAME);
    }
}