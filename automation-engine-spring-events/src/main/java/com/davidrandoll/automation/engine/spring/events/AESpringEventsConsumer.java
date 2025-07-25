package com.davidrandoll.automation.engine.spring.events;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.spring.events.properties.AESpringEventsEnabled;
import com.davidrandoll.automation.engine.spring.events.properties.AESpringEventsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Conditional(AESpringEventsEnabled.class)
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
    @EventListener
    public void consumeAllEvents(Object event) {
        if (event instanceof IEvent || event instanceof EventContext) return; // already handled by AutomationEngine
        var shouldPublish = properties.getAllowedEventTypes().stream()
                .noneMatch(pattern -> event.getClass().getName().matches(pattern));
        if (shouldPublish) return;
        var iEvent = engine.getEventFactory().createEvent(event);
        //engine.publishEvent(iEvent);
    }
}