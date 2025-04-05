package com.automation.engine.core;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.events.IEvent;
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

    public void addAutomation(Automation automation) {
        automations.add(automation);
    }

    public void removeAutomation(Automation automation) {
        automations.remove(automation);
    }

    public void clearAutomations() {
        automations.clear();
    }

    public void processEvent(@NonNull EventContext eventContext) {
        for (Automation automation : automations) {
            runAutomation(automation, eventContext);
        }
    }

    public void processEvent(@NonNull IEvent event) {
        for (Automation automation : automations) {
            runAutomation(automation, EventContext.of(event));
        }
    }

    public void publishEvent(@NonNull IEvent event) {
        var context = EventContext.of(event);
        processEvent(context);
        publisher.publishEvent(event); //publish the event
        publisher.publishEvent(context); //publish the context
    }

    public void runAutomation(Automation automation, EventContext eventContext) {
        log.debug("Processing automation: {}", automation.getAlias());
        automation.resolveVariables(eventContext);
        if (automation.anyTriggerActivated(eventContext) && automation.allConditionsMet(eventContext)) {
            log.debug("Automation triggered and conditions met. Executing actions.");
            automation.performActions(eventContext);
        }
        log.debug("Done processing automation: {}", automation.getAlias());
    }
}