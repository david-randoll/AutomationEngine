package com.automation.engine.core.events.publisher;

@FunctionalInterface
public interface IEventPublisher {
    void publishEvent(Object event);
}
