package com.davidrandoll.automation.engine.spring.events;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.IEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;

@RequiredArgsConstructor
public class SpringEventConsumer {
    private final AutomationEngine engine;

    /**
     * Receives all spring events and publishes them to the AutomationEngine.
     *
     * @param event the event to consume
     */
    @EventListener
    public void consumeAllEvents(Object event) {
        if (event instanceof IEvent) return; // already handled by AutomationEngine
        IEvent iEvent = engine.getEventFactory().createEvent(event);
        engine.publishEvent(iEvent);
    }
}