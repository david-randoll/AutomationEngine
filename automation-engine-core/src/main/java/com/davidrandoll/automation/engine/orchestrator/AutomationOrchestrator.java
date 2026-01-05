package com.davidrandoll.automation.engine.orchestrator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.events.publisher.*;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.state.IStateStore;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
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
        
        // FIRST: Check if this event should resume any paused executions
        resumePausedExecutionsIfTriggered(eventContext);
        
        // THEN: Process normal automation triggers
        for (Automation automation : automations) {
            executionFunction.accept(automation, eventContext);
        }
        publisher.publishEvent(eventContext.getEvent()); //publish the event
        publisher.publishEvent(eventContext); //publish the context
    }

    @Override
    public AutomationResult executeAutomation(Automation automation, EventContext eventContext) {
        AutomationResult result;

        // Store automation ID in metadata for resumption
        eventContext.getMetadata().put("_automationId", automation.getId());

        automation.resolveVariables(eventContext);
        if (automation.anyTriggerActivated(eventContext) && automation.allConditionsMet(eventContext)) {
            log.debug("Automation triggered and conditions met. Executing actions.");
            ActionResult actionResult = automation.performActions(eventContext);

            var executionSummary = automation.getExecutionSummary(eventContext);
            if (actionResult == ActionResult.PAUSE) {
                log.debug("Automation paused. Saving state.");
                stateStore.save(eventContext);
                result = AutomationResult.paused(automation, eventContext, executionSummary);
            } else if (actionResult.isPause() && actionResult.resumeTrigger() != null) {
                log.debug("Automation paused with resume trigger. Saving state.");
                // Store trigger and timeout in metadata
                eventContext.getMetadata().put("_resumeTrigger", actionResult.resumeTrigger());
                if (actionResult.timeoutMillis() != null) {
                    eventContext.getMetadata().put("_timeoutMillis", actionResult.timeoutMillis());
                }
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

    /**
     * Checks all paused executions to see if any should be resumed by the incoming event.
     * This method is called before normal trigger processing to allow paused automations
     * to be woken up by matching events.
     */
    private void resumePausedExecutionsIfTriggered(EventContext incomingEventContext) {
        List<UUID> executionsToResume = new ArrayList<>();
        List<UUID> timedOutExecutions = new ArrayList<>();
        
        long currentTimeMillis = System.currentTimeMillis();
        
        // Check all paused executions
        for (EventContext pausedContext : stateStore.findAll()) {
            UUID executionId = pausedContext.getExecutionId();
            ITrigger resumeTrigger = (ITrigger) pausedContext.getMetadata().get("_resumeTrigger");
            
            // Check for timeout
            Long pausedAtMillis = (Long) pausedContext.getMetadata().get("_pausedAtMillis");
            Long timeoutMillis = (Long) pausedContext.getMetadata().get("_timeoutMillis");
            
            if (timeoutMillis != null && pausedAtMillis != null) {
                long elapsed = currentTimeMillis - pausedAtMillis;
                if (elapsed > timeoutMillis) {
                    String pauseId = (String) pausedContext.getMetadata().get("_pauseId");
                    if (pauseId != null) {
                        log.warn("Execution {} (pauseId: {}) timed out after {} ms waiting for trigger", 
                                 executionId, pauseId, elapsed);
                    } else {
                        log.warn("Execution {} timed out after {} ms waiting for trigger", executionId, elapsed);
                    }
                    timedOutExecutions.add(executionId);
                    continue;
                }
            }
            
            // Check if resume trigger is activated by incoming event
            if (resumeTrigger != null) {
                try {
                    // Create an empty trigger context for evaluation
                    TriggerContext triggerContext = new TriggerContext();
                    triggerContext.setTrigger("resume");
                    
                    if (resumeTrigger.isTriggered(incomingEventContext, triggerContext)) {
                        String pauseId = (String) pausedContext.getMetadata().get("_pauseId");
                        if (pauseId != null) {
                            log.info("Resume trigger activated for execution {} (pauseId: {})", 
                                     executionId, pauseId);
                        } else {
                            log.info("Resume trigger activated for execution {}", executionId);
                        }
                        
                        // Merge incoming event data into paused context for resumption
                        pausedContext.getMetadata().putAll(incomingEventContext.getMetadata());
                        pausedContext.getMetadata().put("_resumeEvent", incomingEventContext.getEvent());
                        
                        executionsToResume.add(executionId);
                    }
                } catch (Exception e) {
                    log.error("Error evaluating resume trigger for execution {}: {}", 
                              executionId, e.getMessage(), e);
                }
            }
        }
        
        // Remove timed out executions
        timedOutExecutions.forEach(stateStore::remove);
        
        // Resume matching executions
        executionsToResume.forEach(this::resumeAutomation);
    }

    @Override
    public AutomationResult resumeAutomation(UUID executionId) {
        var contextOptional = stateStore.findById(executionId);
        if (contextOptional.isEmpty()) {
            log.warn("Could not find state for execution ID: {}", executionId);
            return null;
        }

        EventContext eventContext = contextOptional.get();
        // Find the automation by ID
        String automationId = (String) eventContext.getMetadata().get("_automationId");
        if (automationId == null) {
            log.error("Automation ID not found in EventContext metadata for execution ID: {}", executionId);
            return null;
        }

        Automation automation = automations.stream()
                .filter(a -> a.getId().equals(automationId))
                .findFirst()
                .orElse(null);

        if (automation == null) {
            log.error("Automation with ID '{}' not found for execution ID: {}", automationId, executionId);
            return null;
        }

        log.debug("Resuming automation '{}' for execution ID: {}", automation.getAlias(), executionId);
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