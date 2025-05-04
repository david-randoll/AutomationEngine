package com.davidrandoll.automation.engine.core.events.publisher;

@FunctionalInterface
public interface IEventPublisher {
    void publishEvent(Object event);
}
