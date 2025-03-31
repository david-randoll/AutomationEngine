package com.automation.engine.core;

import com.automation.engine.core.events.Event;
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

    public void processEvent(@NonNull Event event) {
        for (Automation automation : automations) {
            runAutomation(automation, event);
        }
    }

    public void publishEvent(@NonNull Event event) {
        processEvent(event);
        publisher.publishEvent(event);
    }

    public void runAutomation(Automation automation, Event event) {
        log.debug("Processing automation: {}", automation.getAlias());
        automation.setVariables(event);
        if (automation.anyTriggerActivated(event) && automation.allConditionsMet(event)) {
            log.debug("Automation triggered and conditions met. Executing actions.");
            automation.performActions(event);
        }
        log.debug("Done processing automation: {}", automation.getAlias());
    }
}