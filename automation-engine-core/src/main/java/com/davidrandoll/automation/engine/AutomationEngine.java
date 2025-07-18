package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.AutomationHandler;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.creator.events.EventFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutomationEngine {
    private final AutomationHandler handler;
    private final AutomationFactory factory;

    @Getter
    private final EventFactory eventFactory;

    public void register(Automation automation) {
        handler.registerAutomation(automation);
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
        handler.removeAutomation(automation);
    }

    public void removeAll() {
        handler.removeAllAutomations();
    }

    public void publishEvent(EventContext eventContext) {
        handler.handleEventContext(eventContext);
    }

    public void publishEvent(IEvent event) {
        handler.handleEvent(event);
    }

    public AutomationResult executeAutomation(Automation automation, EventContext eventContext) {
        return handler.executeAutomation(automation, eventContext);
    }

    public AutomationResult executeAutomationWithYaml(String yaml, EventContext eventContext) {
        var automation = factory.createAutomation("yaml", yaml);
        return handler.executeAutomation(automation, eventContext);
    }

    public AutomationResult executeAutomationWithYaml(String yaml, IEvent event) {
        return executeAutomationWithYaml(yaml, new EventContext(event));
    }

    public AutomationResult executeAutomationWithJson(String json, EventContext eventContext) {
        var automation = factory.createAutomation("json", json);
        return handler.executeAutomation(automation, eventContext);
    }

    public AutomationResult executeAutomationWithJson(String json, IEvent event) {
        return executeAutomationWithJson(json, new EventContext(event));
    }
}