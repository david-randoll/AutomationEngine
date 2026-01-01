package com.davidrandoll.automation.engine.orchestrator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.events.publisher.*;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.state.IStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class AutomationOrchestrator implements IAEOrchestrator {
    private final IEventPublisher publisher;
    private final IStateStore stateStore;
    private final List<Automation> automations = new CopyOnWriteArrayList<>();

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

        // Store automation alias in metadata for resumption
        eventContext.getMetadata().put("_automationAlias", automation.getAlias());

        automation.resolveVariables(eventContext);
        if (automation.anyTriggerActivated(eventContext) && automation.allConditionsMet(eventContext)) {
            log.debug("Automation triggered and conditions met. Executing actions.");
            ActionResult actionResult = automation.performActions(eventContext);

            var executionSummary = automation.getExecutionSummary(eventContext);
            if (actionResult == ActionResult.PAUSE) {
                log.debug("Automation paused. Saving state.");
                stateStore.save(eventContext);
                result = AutomationResult.paused(automation, eventContext, executionSummary);
            } else {
                result = AutomationResult.executed(automation, eventContext, executionSummary);
            }
        } else {
            log.debug("Automation not triggered or conditions not met. Skipping actions.");
            result = AutomationResult.skipped(automation, eventContext);
        }
        publisher.publishEvent(new AutomationEngineProcessedEvent(automation, eventContext, result));
        return result;
    }

    @Override
    public AutomationResult resumeAutomation(UUID executionId) {
        var contextOptional = stateStore.findById(executionId);
        if (contextOptional.isEmpty()) {
            log.warn("Could not find state for execution ID: {}", executionId);
            return null;
        }

        EventContext eventContext = contextOptional.get();
        // Find the automation by alias (assuming alias is stored in metadata or we need to add it to EventContext)
        // For now, let's assume we can find it.
        // Actually, we should probably store the automation alias in the EventContext.
        String automationAlias = (String) eventContext.getMetadata().get("_automationAlias");
        if (automationAlias == null) {
            log.error("Automation alias not found in EventContext metadata for execution ID: {}", executionId);
            return null;
        }

        Automation automation = automations.stream()
                .filter(a -> a.getAlias().equals(automationAlias))
                .findFirst()
                .orElse(null);

        if (automation == null) {
            log.error("Automation with alias '{}' not found for execution ID: {}", automationAlias, executionId);
            return null;
        }

        log.debug("Resuming automation '{}' for execution ID: {}", automationAlias, executionId);
        ActionResult actionResult = automation.performActions(eventContext);

        if (actionResult == ActionResult.PAUSE) {
            log.debug("Automation paused again. Updating state.");
            stateStore.save(eventContext);
        } else {
            log.debug("Automation completed. Removing state.");
            stateStore.remove(executionId);
        }

        var executionSummary = automation.getExecutionSummary(eventContext);
        AutomationResult result = AutomationResult.executed(automation, eventContext, executionSummary);
        publisher.publishEvent(new AutomationEngineProcessedEvent(automation, eventContext, result));
        return result;
    }
}