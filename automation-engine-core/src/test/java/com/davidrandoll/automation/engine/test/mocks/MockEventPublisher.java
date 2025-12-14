package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.events.publisher.IEventPublisher;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of IEventPublisher for testing.
 * Tracks all published events for verification.
 */
@Getter
public class MockEventPublisher implements IEventPublisher {
    private final List<Object> publishedEvents = new ArrayList<>();

    public void clear() {
        publishedEvents.clear();
    }

    public int getEventCount() {
        return publishedEvents.size();
    }

    public <T> List<T> getEventsOfType(Class<T> eventType) {
        return publishedEvents.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .toList();
    }

    @Override
    public void publishEvent(Object event) {
        publishedEvents.add(event);
    }
}
