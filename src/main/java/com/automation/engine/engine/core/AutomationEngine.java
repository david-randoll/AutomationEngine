package com.automation.engine.engine.core;

import com.automation.engine.engine.core.events.Event;
import com.automation.engine.engine.core.events.EventContext;
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
            EventContext context = event.getContext();
            if (automation.isTriggered(event) && automation.areConditionsMet(context)) {
                log.debug("Automation triggered and conditions met. Executing actions.");
                automation.executeActions(context);
            }
        }
    }
}