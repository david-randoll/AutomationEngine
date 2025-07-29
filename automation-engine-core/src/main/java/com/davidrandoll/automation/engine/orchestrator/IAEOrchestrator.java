package com.davidrandoll.automation.engine.orchestrator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface IAEOrchestrator {
    List<Automation> getAutomations();
    void registerAutomation(Automation automation);
    void removeAutomation(Automation automation);
    void removeAllAutomations();
    void handleEventContext(EventContext eventContext);
    void handleEvent(IEvent event);
    void handleEvent(EventContext eventContext,BiConsumer<Automation, EventContext> executionFunction);
    AutomationResult executeAutomation(Automation automation, EventContext eventContext);
}