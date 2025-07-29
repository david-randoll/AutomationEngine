package com.davidrandoll.automation.engine.orchestrator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;

import java.util.List;

public interface IAEOrchestrator {
    List<Automation> getAutomations();
    void registerAutomation(Automation automation);
    void removeAutomation(Automation automation);
    void removeAllAutomations();
    void handleEventContext(EventContext eventContext);
    void handleEvent(IEvent event);
    AutomationResult executeAutomation(Automation automation, EventContext eventContext);
}