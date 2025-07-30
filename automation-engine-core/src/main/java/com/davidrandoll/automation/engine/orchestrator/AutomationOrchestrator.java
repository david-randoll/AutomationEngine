package com.davidrandoll.automation.engine.orchestrator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.events.publisher.*;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class AutomationOrchestrator implements IAEOrchestrator {
    private final IEventPublisher publisher;
    private final List<Automation> automations = new CopyOnWriteArrayList<>();

    /**
     * Retrieves a copy of the list of automations.
     * <p>
     * This method is thread-safe because the underlying list is a {@link CopyOnWriteArrayList},
     * which ensures safe concurrent access. A copy of the list is returned to prevent
     * accidental modifications to the original list by the caller.
     * <p>
     * An unmodifiable view was not chosen because the caller might need to perform operations
     * on the returned list without affecting the original list.
     *
     * @return A copy of the list of automations.
     */
    @Override
    public List<Automation> getAutomations() {
        return new ArrayList<>(automations);
    }

    @Override
    public void registerAutomation(Automation automation) {
        automations.add(automation);
        publisher.publishEvent(new AutomationEngineRegisterEvent(automation));
    }

    @Override
    public void removeAutomation(Automation automation) {
        automations.remove(automation);
        publisher.publishEvent(new AutomationEngineRemoveEvent(automation));
    }

    @Override
    public void removeAllAutomations() {
        var automationsCopy = new ArrayList<>(automations);
        automations.clear();
        publisher.publishEvent(new AutomationEngineRemoveAllEvent(automationsCopy));
    }

    @Override
    public void handleEventContext(EventContext eventContext) {
        this.handleEvent(eventContext, this::executeAutomation);
    }

    @Override
    public void handleEvent(IEvent event) {
        this.handleEvent(EventContext.of(event), this::executeAutomation);
    }

    @Override
    public void handleEvent(EventContext eventContext, BiConsumer<Automation, EventContext> executionFunction) {
        if (eventContext == null) throw new IllegalArgumentException("EventContext cannot be null");
        if (eventContext.getEvent() == null) throw new IllegalArgumentException("Event cannot be null");
        for (Automation automation : automations) {
            executionFunction.accept(automation, eventContext);
        }
        publisher.publishEvent(eventContext.getEvent()); //publish the event
        publisher.publishEvent(eventContext); //publish the context
    }

    @Override
    public AutomationResult executeAutomation(Automation automation, EventContext eventContext) {
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
        return result;
    }
}