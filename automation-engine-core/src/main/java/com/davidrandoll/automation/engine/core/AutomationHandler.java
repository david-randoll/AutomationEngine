package com.davidrandoll.automation.engine.core;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.events.publisher.*;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RequiredArgsConstructor
public class AutomationHandler {
    private final IEventPublisher publisher;
    private final List<Automation> automations = new CopyOnWriteArrayList<>();

    public void registerAutomation(Automation automation) {
        automations.add(automation);
        publisher.publishEvent(new AutomationEngineRegisterEvent(automation));
    }

    public void removeAutomation(Automation automation) {
        automations.remove(automation);
        publisher.publishEvent(new AutomationEngineRemoveEvent(automation));
    }

    public void removeAllAutomations() {
        var automationsCopy = new ArrayList<>(automations);
        automations.clear();
        publisher.publishEvent(new AutomationEngineRemoveAllEvent(automationsCopy));
    }

    public void handleEventContext(EventContext eventContext) {
        if (eventContext == null) throw new IllegalArgumentException("EventContext cannot be null");
        for (Automation automation : automations) {
            executeAutomation(automation, eventContext);
        }
        publisher.publishEvent(eventContext.getEvent()); //publish the event
        publisher.publishEvent(eventContext); //publish the context
    }

    public void handleEvent(IEvent event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        for (Automation automation : automations) {
            executeAutomation(automation, EventContext.of(event));
        }
        publisher.publishEvent(event); //publish the event
        publisher.publishEvent(EventContext.of(event)); //publish the context
    }

    public AutomationResult executeAutomation(Automation automation, EventContext eventContext) {
        log.debug("Processing automation: {}", automation.getAlias());
        AutomationResult result;
        automation.resolveVariables(eventContext);
        if (automation.anyTriggerActivated(eventContext) && automation.allConditionsMet(eventContext)) {
            log.debug("Automation triggered and conditions met. Executing actions.");
            automation.performActions(eventContext);
            var executionSummary = automation.getExecutionSummary(eventContext);
            result = AutomationResult.executed(automation, eventContext, executionSummary);
        } else {
            log.debug("Automation not triggered or conditions not met. Skipping actions.");
            result = AutomationResult.skipped(automation, eventContext);
        }
        publisher.publishEvent(new AutomationEngineProcessedEvent(automation, eventContext, result));
        log.debug("Done processing automation: {}", automation.getAlias());
        return result;
    }
}