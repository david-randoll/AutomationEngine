package com.automation.engine.modules;

import com.automation.engine.core.events.Event;
import com.automation.engine.modules.time_based.event.TimeBasedEvent;

import java.util.Map;

public class DefaultEvent extends Event {
    public static final String EVENT_NAME = TimeBasedEvent.class.getSimpleName();

    public DefaultEvent() {
        super(EVENT_NAME, Map.of());
    }

    public DefaultEvent(String name, Map<String, Object> data) {
        super(name, data);
    }
}
