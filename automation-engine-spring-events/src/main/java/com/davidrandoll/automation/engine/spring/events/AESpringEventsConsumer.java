package com.davidrandoll.automation.engine.spring.events;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.spring.events.properties.AESpringEventsProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AESpringEventsConsumer {
    private final AutomationEngine engine;
    private final AESpringEventsProperties properties;

    /**
     * Receives all spring events and publishes them to the AutomationEngine.
     * The events are filtered based on the allowed event types defined in the properties.
     * This is to prevent unwanted events from being processed by the AutomationEngine. And it also prevents
     * republishing of events that are already handled by the AutomationEngine (like IEvent and EventContext).
     *
     * @param event the event to consume
     */
    // TODO: come back to this. There is essentially an infinite loop with the tests and events getting resubmitted back into the automation engine.
//    @EventListener
//    public void consumeAllEvents(Object event) {
//        if (event instanceof IEvent || event instanceof EventContext || event instanceof AutomationOrigin)
//            return; // already handled by AutomationEngine
//        var shouldPublish = properties.getAllowedEventTypes().stream()
//                .noneMatch(pattern -> event.getClass().getName().matches(pattern));
//        if (shouldPublish) return;
//        var iEvent = engine.getEventFactory().createEvent(event);
//        engine.publishEvent(iEvent);
//    }
}