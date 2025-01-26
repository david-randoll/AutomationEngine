package com.automation.engine.core;

import com.automation.engine.core.events.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationEngine {
    private final List<Automation> automations = new ArrayList<>();

    public void addAutomation(Automation automation) {
        automations.add(automation);
    }

    public void removeAutomation(Automation automation) {
        automations.remove(automation);
    }

    public void processEvent(@NonNull Event event) {
        for (Automation automation : automations) {
            log.debug("Processing automation: {}", automation.getAlias());
            if (automation.anyTriggerActivated(event) && automation.allConditionsMet(event)) {
                log.debug("Automation triggered and conditions met. Executing actions.");
                automation.performActions(event);
            }
        }
    }
}