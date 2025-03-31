package com.automation.engine.modules.events.time_based;

import com.automation.engine.core.events.Event;
import org.springframework.lang.NonNull;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

public class TimeBasedEvent extends Event {
    public static final String EVENT_NAME = TimeBasedEvent.class.getSimpleName();
    private static final String FIELD_NAME = "time";

    public TimeBasedEvent(@NonNull LocalTime time) {
        super(EVENT_NAME, Map.of(FIELD_NAME, time));
    }

    public TimeBasedEvent() {
        super(EVENT_NAME, Map.of());
    }

    @NonNull
    public LocalTime getTime() {
        var time = (LocalTime) getData().get(FIELD_NAME);
        return Optional.ofNullable(time).orElse(LocalTime.now());
    }
}