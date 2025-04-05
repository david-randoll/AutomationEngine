package com.automation.engine.core.events.publisher;

import com.automation.engine.core.Automation;
import com.automation.engine.core.events.EventContext;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationEngineProcessedEvent {
    private Automation automation;
    private EventContext eventContext;
}