package com.davidrandoll.automation.engine.test;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventCaptureListener {
    private final List<Object> events = new CopyOnWriteArrayList<>();

    // where it is only from com.davidrandoll
    @EventListener(condition = "#event.class.name.startsWith('com.davidrandoll')")
    public void onEvent(Object event) {
        events.add(event);
    }

    public List<Object> getEvents() {
        return events;
    }

    public void clearEvents() {
        events.clear();
    }
}

