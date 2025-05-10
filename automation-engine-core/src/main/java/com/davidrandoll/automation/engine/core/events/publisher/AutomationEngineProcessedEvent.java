package com.davidrandoll.automation.engine.core.events.publisher;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import lombok.Data;

@Data
public class AutomationEngineProcessedEvent {
    private Automation automation;
    private EventContext eventContext;
    private AutomationResult result;

    public AutomationEngineProcessedEvent(Automation automation, EventContext eventContext, AutomationResult result) {
        this.automation = new Automation(automation);
        this.eventContext = eventContext;
        this.result = result;
    }
}