package com.automation.engine.core;

import com.automation.engine.core.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationEngine {
    private final ApplicationEventPublisher publisher;
    private final List<Automation> automations = new ArrayList<>();

    public void register(Automation automation) {
        automations.add(automation);
        publisher.publishEvent(new AutomationEngineRegisterEvent(automation));
    }

    public void remove(Automation automation) {
        automations.remove(automation);
        publisher.publishEvent(new AutomationEngineRemoveEvent(automation));
    }

    public void removeAll() {
        //automations.forEach(this::remove);
        automations.clear();
    }

    public void publishEvent(@NonNull EventContext eventContext) {
        for (Automation automation : automations) {
            runAutomation(automation, eventContext);
        }
        publisher.publishEvent(eventContext.getEvent()); //publish the event
        publisher.publishEvent(eventContext); //publish the context
    }

    public void publishEvent(@NonNull IEvent event) {
        for (Automation automation : automations) {
            runAutomation(automation, EventContext.of(event));
        }
        publisher.publishEvent(event); //publish the event
        publisher.publishEvent(EventContext.of(event)); //publish the context
    }

    public void runAutomation(Automation automation, EventContext eventContext) {
        log.debug("Processing automation: {}", automation.getAlias());
        automation.resolveVariables(eventContext);
        if (automation.anyTriggerActivated(eventContext) && automation.allConditionsMet(eventContext)) {
            log.debug("Automation triggered and conditions met. Executing actions.");
            automation.performActions(eventContext);
        }
        publisher.publishEvent(new AutomationEngineProcessedEvent(automation, eventContext));
        log.debug("Done processing automation: {}", automation.getAlias());
    }
}