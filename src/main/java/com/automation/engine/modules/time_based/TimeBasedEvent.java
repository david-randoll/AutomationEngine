package com.automation.engine.modules.time_based;

import com.automation.engine.engine.core.events.Event;
import org.springframework.lang.NonNull;

import java.time.LocalTime;
import java.util.Map;

public class TimeBasedEvent extends Event {
    public static final String EVENT_NAME = "TimeEvent";
    private static final String FIELD_NAME = "time";

    public TimeBasedEvent(@NonNull LocalTime time) {
        super(EVENT_NAME, Map.of(FIELD_NAME, time));
    }

    @NonNull
    public LocalTime getTime() {
        return (LocalTime) getData().get(FIELD_NAME);
    }
}