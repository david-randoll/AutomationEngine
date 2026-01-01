package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.creator.events.EventFactory;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AutomationEngine {
    private final IAEOrchestrator orchestrator;
    private final AutomationFactory factory;

    @Getter
    private final EventFactory eventFactory;

    public void register(Automation automation) {
        orchestrator.registerAutomation(automation);
    }

    public void registerWithYaml(String yaml) {
        var automation = factory.createAutomation("yaml", yaml);
        register(automation);
    }

    public void registerWithJson(String json) {
        var automation = factory.createAutomation("json", json);
        register(automation);
    }

    public void remove(Automation automation) {
        orchestrator.removeAutomation(automation);
    }

    public void removeAll() {
        orchestrator.removeAllAutomations();
    }

    public void publishEvent(EventContext eventContext) {
        orchestrator.handleEventContext(eventContext);
    }

    public void publishEvent(IEvent event) {
        orchestrator.handleEvent(event);
    }

    public AutomationResult resumeAutomation(UUID executionId) {
        return orchestrator.resumeAutomation(executionId);
    }

    public AutomationResult executeAutomation(Automation automation, EventContext eventContext) {
        return orchestrator.executeAutomation(automation, eventContext);
    }

    public AutomationResult executeAutomationWithYaml(String yaml, EventContext eventContext) {
        var automation = factory.createAutomation("yaml", yaml);
        return orchestrator.executeAutomation(automation, eventContext);
    }

    public AutomationResult executeAutomationWithYaml(String yaml, IEvent event) {
        return executeAutomationWithYaml(yaml, new EventContext(event));
    }

    public AutomationResult executeAutomationWithJson(String json, EventContext eventContext) {
        var automation = factory.createAutomation("json", json);
        return orchestrator.executeAutomation(automation, eventContext);
    }

    public AutomationResult executeAutomationWithJson(String json, IEvent event) {
        return executeAutomationWithJson(json, new EventContext(event));
    }
}